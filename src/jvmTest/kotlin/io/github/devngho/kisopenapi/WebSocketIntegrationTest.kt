package io.github.devngho.kisopenapi

import io.github.devngho.kisopenapi.KISApiClient.Companion.options
import io.github.devngho.kisopenapi.requests.util.json
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.should
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.logging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import java.io.FileNotFoundException

val apiForIntegration: KISApiClient by lazy {
    runBlocking {
        val key: List<String>
        val account: List<String>
        val token: KISApiClient.KISApiTokens
        val htsId: List<String>

        try {
            key = readLines("key.txt")
            account = readLines("account.txt")
            token = kotlin.runCatching {
                json.decodeFromString<KISApiClient.KISApiTokens>(
                    readLines("token.txt").joinToString("")
                )
            }.getOrDefault(KISApiClient.KISApiTokens("", ""))
            token.issueIfExpired()
            htsId = readLines("id.txt")
        } catch (e: FileNotFoundException) {
            throw FileNotFoundException("테스트를 위해 key.txt, account.txt, id.txt와 빈 token.txt 파일을 작성해주세요.")

            /*
            key.txt: 첫 줄에 appKey, 둘째 줄에 appSecret을 작성해주세요.
            account.txt: 첫 줄에 계좌번호를 작성해주세요.
            id: 첫 줄에 HTS ID를 작성해주세요.
             */
        }

        if (!token.isExpired) {
            KISApiClient.withToken(
                token,
                key[0],
                key[1],
                false,
                account = account[0],
                id = htsId[0]
            ).options {
//                useHashKey = true
                autoReconnect = true
                webSocketClient = WebSocketTest.WebSocketMockClient(webSocketClient)
                webSocketUrl = "ws://localhost:8080"
                webSocketReceiveTimeout = 1
            }
        } else {
            KISApiClient.with(
                key[0], key[1], false, account = account[0], id = htsId[0]
            ).options {
//                useHashKey = true
                autoReconnect = true
                webSocketClient = WebSocketTest.WebSocketMockClient(webSocketClient)
                webSocketUrl = "ws://localhost:8080"
                webSocketReceiveTimeout = 1
            }.apply {
                writeText(
                    "token.txt",
                    json.encodeToString(this.tokens)
                )
            }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
class WebSocketIntegrationTest : ShouldSpec({
    val logger = KtorSimpleLogger("WebSocketIntegrationTest")

    GlobalScope.launch {
        apiForIntegration.webSocket.eventFlow.collect {
            logger.info(it.toString())
        }
    }

    GlobalScope.launch {
        apiForIntegration.webSocket.incoming.collect {
            logger.info(it)
        }
    }

    embeddedServer(CIO, port = 8080) {
        install(WebSockets)

        routing {
            webSocket {
                try {
                    logger.info("WebSocket opened")
                    for (frame in incoming) {
                        outgoing.send(frame)
                    }
                } finally {
                    logger.info("WebSocket closed")
                }
            }
        }
    }.start()

    beforeEach {
        apiForIntegration.webSocket.buildWebsocket()
        logger.info("setup test websocket done")
    }

    context("Timeout") {
        should("timeout") {
            val ch = Channel<Unit>()

            val job = launch {
                apiForIntegration.webSocket.eventFlow.filter { (it is KISApiClient.WebSocket.Event.OnClose && it.reason?.message == "receiveTimeout") }
                    .collect {
                        ch.send(Unit)
                    }
            }

            select {
                onTimeout(2000) {
                    throw Exception("Test failed")
                }

                ch.onReceive {}
            }

            job.cancel()
        }

        should("reconnect") {
            apiForIntegration.options.autoReconnect = true
            val events = mutableListOf<KISApiClient.WebSocket.Event>()

            val job = launch {
                apiForIntegration.webSocket.eventFlow.collect {
                    events.add(it)
                }
            }

            delay(1500)

            // timeout(close) -> open

            job.cancel()

            events.first() should { it is KISApiClient.WebSocket.Event.OnClose && it.reason?.message == "receiveTimeout" }
            events.last() should { it is KISApiClient.WebSocket.Event.OnOpen }
        }
    }
})