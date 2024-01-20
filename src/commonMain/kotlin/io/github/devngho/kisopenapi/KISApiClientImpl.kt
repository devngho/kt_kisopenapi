package io.github.devngho.kisopenapi

import io.github.devngho.kisopenapi.requests.LiveData
import io.github.devngho.kisopenapi.requests.LiveRequest
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.ratelimit.ClockRateLimiter
import io.github.devngho.kisopenapi.requests.response.LiveResponse
import io.github.devngho.kisopenapi.requests.util.InternalApi
import io.github.devngho.kisopenapi.requests.util.WebSocketSubscribed
import io.github.devngho.kisopenapi.requests.util.createHttpClient
import io.github.devngho.kisopenapi.requests.util.json
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Suppress("SpellCheckingInspection")
@OptIn(InternalApi::class)
class KISApiClientImpl internal constructor(
    override val appKey: String,
    override val appSecret: String,
    override val isDemo: Boolean,
    override var account: Pair<String, String>?,
    override var htsId: String? = null,
    override val corpRequest: CorporationRequest?,
    override val tokens: KISApiClient.KISApiTokens,
) : KISApiClient {
    override val httpClient = createHttpClient()

    override val options: KISApiClient.KISApiOptions = KISApiClient.KISApiOptions(
        rateLimiter = ClockRateLimiter.byDefaultRate(isDemo),
        webSocketClient = WebSocketImpl(this),
        webSocketManager = WebSocketManagerImpl(this),
        baseUrl = if (isDemo) "https://openapivts.koreainvestment.com:29443" else "https://openapi.koreainvestment.com:9443",
        webSocketUrl = if (isDemo) "ws://ops.koreainvestment.com:31000" else "ws://ops.koreainvestment.com:21000"
    )

    override val webSocket: KISApiClient.WebSocket
        get() = options.webSocketClient

    override val webSocketManager: KISApiClient.WebSocketManager
        get() = options.webSocketManager

    class WebSocketManagerImpl(
        val client: KISApiClient
    ) : KISApiClient.WebSocketManager {
        /**
         * tr_key 목록입니다.
         * 구독하거나 구독을 해제할 때, 다른 곳에서 구독 중인지 확인하기 위해 사용합니다.
         * @see io.github.devngho.kisopenapi.requests.LiveRequest
         */
        private val webSocketSubscribedKey = mutableListOf<String>()
        private val webSocketSubscribedKeyMutex = Mutex()

        /**
         * tr_key에 대한 구독 완료 여부와 결과입니다.
         * 구독된 요청을 다시 구독할 때, 같은 [LiveResponse]를 사용해 요청의 init 함수를 실행하기 위해 사용합니다.
         * @see io.github.devngho.kisopenapi.requests.LiveRequest
         */
        private val webSocketSubscribedInit = mutableMapOf<String, Deferred<LiveResponse>>()
        private val webSocketSubscribedInitMutex = Mutex()

        /**
         * 구독 중인 요청 목록입니다.
         * 구독 상태를 복원하기 위해 사용합니다.
         * @see io.github.devngho.kisopenapi.requests.LiveRequest
         */
        private val webSocketSubscribed = mutableListOf<WebSocketSubscribed>()
        private val webSocketSubscribedMutex = Mutex()

        override fun getSubscribed(): List<WebSocketSubscribed> = webSocketSubscribed.toList()

        override suspend fun subscribeStart(
            request: WebSocketSubscribed,
            force: Boolean,
            onAlreadyDone: suspend (LiveResponse) -> Unit
        ): Boolean {
            val id = request.let { "${it.request::class.simpleName}${it.data.tradeKey(client)}" }

            webSocketSubscribedMutex.withLock {
                webSocketSubscribed.add(request)
            }
            webSocketSubscribedKeyMutex.withLock {
                webSocketSubscribedKey.add(id)

                // 다른 곳에서 이미 구독 중인 경우, 그 구독 완료 정보를 가져옵니다.
                // 아직 구독 완료 정보가 없는 경우, 구독 완료 정보가 들어올 때까지 대기합니다.
                if (webSocketSubscribedKey.count { c -> c == id } > 1 && !force) {
                    onAlreadyDone(webSocketSubscribedInit[id]!!.await())
                    return false
                }
            }

            webSocketSubscribedInitMutex.withLock {
                // 구독 완료 정보를 대기하기 위한 CompletableDeferred를 생성합니다.
                webSocketSubscribedInit[id] = CompletableDeferred()
            }

            return true
        }

        override suspend fun subscribeDone(request: WebSocketSubscribed, response: LiveResponse) {
            val id = request.let { "${it.request::class.simpleName}${it.data.tradeKey(client)}" }

            webSocketSubscribedInitMutex.withLock {
                webSocketSubscribedInit[id]
                    ?.let {
                        // 구독 완료 정보를 저장합니다.
                        (webSocketSubscribedInit[id] as? CompletableDeferred<LiveResponse>
                            ?: throw IllegalStateException("Subscribed init is not CompletableDeferred.")).complete(
                            response
                        )
                    }
            }
        }

        override suspend fun unsubscribe(request: WebSocketSubscribed): Boolean {
            webSocketSubscribedMutex.withLock {
                webSocketSubscribed.remove(request)
            }
            val id = request.let { "${it.request::class.simpleName}${it.data.tradeKey(client)}" }

            return webSocketSubscribedKeyMutex.withLock {
                webSocketSubscribedKey.remove(id)
                (!webSocketSubscribedKey.contains(id)).also @Suppress("DeferredResultUnused") {
                    if (it) webSocketSubscribedInit.remove(id) // 마지막 구독이 해제되었을 경우, 구독 완료 정보를 제거합니다.
                } // 다른 곳에서 구독 중인지 확인하고, 없으면 구독 해제 요청을 전송해야 합니다.
            }
        }

        @InternalApi
        override suspend fun clearSubscribed() {
            if (webSocketSubscribedKeyMutex.isLocked) webSocketSubscribedKeyMutex.unlock()
            webSocketSubscribedKey.clear()

            while (webSocketSubscribedInitMutex.isLocked) webSocketSubscribedInitMutex.unlock()
            webSocketSubscribedInit.clear()

            while (webSocketSubscribedMutex.isLocked) webSocketSubscribedMutex.unlock()
            webSocketSubscribed.clear()
        }
    }

    class WebSocketImpl(
        val client: KISApiClient,
    ) : KISApiClient.WebSocket {
        private val _incoming: MutableSharedFlow<String> = MutableSharedFlow(1)
        private var session: DefaultClientWebSocketSession? = null

        override var scope: CoroutineScope? = null
        override val incoming: SharedFlow<String> = _incoming.asSharedFlow()
        override val outgoing: SendChannel<String> = Channel(8)
        override val isConnected: Boolean
            get() = session?.isActive ?: false

        private val _eventFlow: MutableSharedFlow<KISApiClient.WebSocket.Event> = MutableSharedFlow()
        override val eventFlow: SharedFlow<KISApiClient.WebSocket.Event> = _eventFlow.asSharedFlow()

        /**
         * 웹소켓이 연결 중이거나 닫히는 중일 때, 다른 곳에서 웹소켓을 연결하거나 닫지 못하도록 합니다.
         */
        private val buildOrCloseMutex = Mutex()

        override suspend fun buildWebsocket() {
            if (isConnected) closeWebsocket()

            buildOrCloseMutex.withLock {
                client.httpClient.webSocketSession(client.options.webSocketUrl)
                    .apply {
                        setupVaraibles()
                        setupSession()

                        _eventFlow.emit(KISApiClient.WebSocket.Event.OnOpen(this))
                    }
            }
        }

        private fun DefaultClientWebSocketSession.setupVaraibles() {
            scope = this
            session = this@setupVaraibles
        }

        private fun DefaultClientWebSocketSession.setupSession() {
            // outgoing 전송
            launch {
                val channel = this@WebSocketImpl.outgoing as Channel<String>

                while (true) {
                    val msg = channel.receive()

                    if (!this@setupSession.isActive) {
                        channel.send(msg)
                        break
                    }

                    _eventFlow.emit(KISApiClient.WebSocket.Event.OnSend(msg))
                    send(Frame.Text(msg))
                }
            }

            // incoming 수신
            launch {
                var e: Exception? = null
                try {
                    for (it in incoming) {
                        processIncoming(it)
                    }
                } catch (ex: Exception) {
                    e = ex
                } finally {
                    withContext(NonCancellable) {
                        val cause = closeReason.await()

                        finalizeSession(cause ?: CloseReason(CloseReason.Codes.NORMAL, ""))

                        _eventFlow.emit(KISApiClient.WebSocket.Event.OnClose(cause, e))
                    }

                    if (e != null) throw e
                }
            }
        }

        /**
         * 옵션에 따라 웹소켓 세션을 종료하거나 재연결합니다.
         */
        private suspend fun finalizeSession(reason: CloseReason) {
            if (client.options.autoReconnect) {
                if (reason.message != "closeWebsocket") reconnect(true)
            } else {
                clearSubscriptions()
                clearSocket()
            }
        }

        /**
         * 웹소켓으로 들어오는 데이터를 처리합니다.
         */
        private suspend fun WebSocketSession.processIncoming(frame: Frame) {
            if (frame is Frame.Text) {
                try {
                    val txt = frame.readText()
                    _incoming.tryEmit(txt)
                    processPingPong(frame, txt)
                } catch (e: Exception) {
                    _eventFlow.tryEmit(KISApiClient.WebSocket.Event.OnError(e))
                }
            }
        }

        /**
         * PINGPONG 메시지를 처리합니다.
         */
        private suspend fun WebSocketSession.processPingPong(frame: Frame, txt: String) {
            if (txt[0] == '0' || txt[0] == '1') return
            val resp = json.decodeFromString<LiveResponse>(txt)
            if (resp.header?.tradeId != "PINGPONG") return

            send(frame)
        }

        private suspend fun clearSubscriptions() = client.options.webSocketManager.clearSubscribed()

        /**
         * 웹소켓 세션을 종료합니다.
         */
        override suspend fun closeWebsocket(): Unit = coroutineScope {
            buildOrCloseMutex.withLock {
                val j = launch { if (isConnected) eventFlow.first { f -> f is KISApiClient.WebSocket.Event.OnClose } }
                clearSubscriptions()
                session?.close(CloseReason(CloseReason.Codes.NORMAL, "closeWebsocket"))
                clearSocket()
                j.join()
            }
        }

        private fun clearSocket() {
            session = null
            scope = null
        }


        override suspend fun reconnectWebsocket() {
            val alreadyClosing = !isConnected

            reconnect(alreadyClosing)
        }

        @Suppress("unchecked_cast")
        private suspend fun reconnect(alreadyClosing: Boolean) = coroutineScope {
            val copiedSubscriptions = client.options.webSocketManager.getSubscribed()

            if (!alreadyClosing) closeWebsocket()

            buildWebsocket()

            // 구독 중인 요청을 다시 구독합니다.
            copiedSubscriptions.forEach {
                (it.request as LiveRequest<LiveData, *>).register(
                    it.data,
                    wait = true,
                    force = true,
                    it.initFunc,
                    it.block
                )
            }
        }
    }
}