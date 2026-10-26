package io.github.devngho.kisopenapi

import io.github.devngho.kisopenapi.KISApiClient.Companion.options
import io.github.devngho.kisopenapi.requests.domestic.inquire.live.InquireLivePrice
import io.github.devngho.kisopenapi.requests.response.LiveCallBody
import io.github.devngho.kisopenapi.requests.response.LiveResponse
import io.github.devngho.kisopenapi.requests.util.RequestCode
import io.github.devngho.kisopenapi.requests.util.Result
import io.github.devngho.kisopenapi.requests.util.json
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.logging.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import kotlinx.serialization.SerializationException
import java.io.FileNotFoundException
import kotlin.reflect.KClass

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

    // 구독 성공 응답을 보내기 전에 먼저 보낼 프레임입니다.
    val preSubscribeFrames = MutableStateFlow(emptyList<String>())

    embeddedServer(CIO, port = 8080) {
        install(WebSockets)

        routing {
            webSocket {
                try {
                    logger.info("WebSocket opened")
                    for (frame in incoming) {
                        // 구독/구독 해제 요청에는 성공 응답을, 그 외에는 받은 프레임을 그대로 돌려보냅니다.
                        val call = (frame as? Frame.Text)?.readText()
                            ?.let { runCatching { json.decodeFromString<LiveCallBody>(it) }.getOrNull() }

                        if (call == null) {
                            outgoing.send(frame)
                            continue
                        }

                        if (call.header.trType == "1") preSubscribeFrames.value.forEach { outgoing.send(Frame.Text(it)) }

                        val code = if (call.header.trType == "1") RequestCode.SubscribeSuccess else RequestCode.UnsubscribeSuccess
                        outgoing.send(
                            Frame.Text(
                                """{"header":{"tr_id":"${call.body.input.trId}","tr_key":"${call.body.input.trKey}","encrypt":"N"},""" +
                                        """"body":{"rt_cd":"0","msg_cd":"${code.code}","msg1":"OK",""" +
                                        """"output":{"iv":"0123456789abcdef","key":"0123456789abcdef0123456789abcdef"}}}"""
                            )
                        )
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

    context("Malformed frame") {
        // GHSA-cc4m-vc9p-jc3r: 잘못된 형식의 프레임 하나로 리스너나 웹소켓 세션이 죽으면 안 됩니다.
        val tradeId = "H0UNCNT0"
        val validBody =
            "$testStock^093354^71900^5^-100^-0.14^72023.83^72100^72400^71700^71900^71800^1^3052507^219853241700^5105^6937^1832^84.90^1366314^1159996^1^0.39^20.28^090020^5^-200^090820^5^-500^092619^2^200^20230612^20^N^65945^216924^1118750^2199206^0.05^2424142^125.92^0^^72100"

        beforeEach {
            apiForIntegration.options.autoReconnect = false
            apiForIntegration.options.webSocketReceiveTimeout = 20
        }

        afterEach {
            preSubscribeFrames.value = emptyList()
            apiForIntegration.options.autoReconnect = true
            apiForIntegration.options.webSocketReceiveTimeout = 1
        }

        /**
         * [frame]을 받은 뒤에도 연결이 유지되고, 이후 들어오는 정상 데이터를 처리하는지 확인합니다.
         * 또한 [expected] 예외를 담은 [KISApiClient.WebSocket.Event.OnError]가 발생했는지 확인합니다.
         *
         * @param expected OnError의 cause(또는 그 cause)로 기대하는 예외 타입
         * @param duringSubscribe true면 구독 응답을 기다리는 중에, false면 구독이 끝난 뒤에 [frame]을 받습니다.
         */
        suspend fun shouldSurvive(
            frame: String,
            expected: KClass<out Throwable>,
            duringSubscribe: Boolean = false
        ) = coroutineScope {
            val closes = mutableListOf<KISApiClient.WebSocket.Event.OnClose>()
            val errors = mutableListOf<KISApiClient.WebSocket.Event.OnError>()
            val eventWatcher = launch {
                apiForIntegration.webSocket.eventFlow.collect {
                    when (it) {
                        is KISApiClient.WebSocket.Event.OnClose -> if (it.reason?.message != "closeWebsocket") closes.add(it)
                        is KISApiClient.WebSocket.Event.OnError -> errors.add(it)
                        else -> {}
                    }
                }
            }

            val instance = InquireLivePrice(apiForIntegration)
            val data = InquireLivePrice.InquireLivePriceData(testStock)
            val received = Channel<InquireLivePrice.InquireLivePriceResponse>(Channel.UNLIMITED)
            var initResult: Result<LiveResponse>? = null

            if (duringSubscribe) preSubscribeFrames.value = listOf(frame)

            try {
                withTimeoutOrNull(3000) {
                    instance.register(data, wait = true, init = { initResult = it }) { received.send(it) }
                }

                if (!duringSubscribe) {
                    withTimeoutOrNull(1000) { apiForIntegration.webSocket.outgoing.send(frame) }
                }

                delay(500)

                withTimeoutOrNull(1000) { apiForIntegration.webSocket.outgoing.send("0|$tradeId|001|$validBody") }

                // then
                initResult?.isOk shouldBe true
                withTimeoutOrNull(2000) { received.receive() }?.ticker shouldBe testStock
                closes.shouldBeEmpty()
                apiForIntegration.webSocket.isConnected shouldBe true
                errors.shouldExist { expected.isInstance(it.cause) || expected.isInstance(it.cause.cause) }
            } finally {
                preSubscribeFrames.value = emptyList()
                withTimeoutOrNull(1000) { instance.unregister(data) }
                eventWatcher.cancel()
            }
        }

        should("빈 프레임을 받아도 연결이 유지된다") {
            shouldSurvive("", IndexOutOfBoundsException::class)
        }

        should("구독 중 빈 프레임을 받아도 구독이 완료된다") {
            shouldSurvive("", IndexOutOfBoundsException::class, duringSubscribe = true)
        }

        should("구분자가 없는 프레임을 받아도 연결이 유지된다") {
            shouldSurvive("0", IndexOutOfBoundsException::class)
        }

        should("구분자가 3개 미만인 프레임을 받아도 연결이 유지된다") {
            shouldSurvive("0|$tradeId|001", IndexOutOfBoundsException::class)
        }

        should("올바르지 않은 Base64 데이터를 받아도 연결이 유지된다") {
            shouldSurvive("1|$tradeId|001|!!not-base64!!", IllegalArgumentException::class)
        }

        should("필드 수가 bodySize의 배수가 아닌 데이터를 받아도 연결이 유지된다") {
            shouldSurvive("0|$tradeId|001|$testStock^093354", IndexOutOfBoundsException::class)
        }

        should("숫자 필드에 숫자가 아닌 값을 받아도 연결이 유지된다") {
            shouldSurvive(
                "0|$tradeId|001|${validBody.replaceFirst("^71900^", "^abc^")}",
                NumberFormatException::class
            )
        }

        should("0/1로 시작하지 않는 JSON이 아닌 프레임을 받아도 연결이 유지된다") {
            shouldSurvive("not json", SerializationException::class)
        }

        should("구독 중 0/1로 시작하지 않는 JSON이 아닌 프레임을 받아도 구독이 완료된다") {
            shouldSurvive("not json", SerializationException::class, duringSubscribe = true)
        }
    }
})