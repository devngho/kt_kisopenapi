package io.github.devngho.kisopenapi

import io.github.devngho.kisopenapi.requests.LiveData
import io.github.devngho.kisopenapi.requests.LiveRequest
import io.github.devngho.kisopenapi.requests.RateLimiter
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.auth.GrantLiveToken
import io.github.devngho.kisopenapi.requests.auth.GrantToken
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.response.LiveResponse
import io.github.devngho.kisopenapi.requests.util.Result
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.jvm.JvmStatic

@Suppress("SpellCheckingInspection")
/**
 * 한국투자증권 API에 접근하기 위한 객체입니다.
 *
 * @property appKey 인증에 사용되는 애플리케이션 키입니다.
 * @property appSecret 인증에 사용되는 애플리케이션 서크릿입니다.
 * @property isDemo 모의 투자 환경을 사용하는지 여부를 나타내는 플래그입니다.
 * @property account 계좌 번호입니다.
 * @property htsId HTS ID입니다.
 * @property corp 개인/법인 정보입니다.
 * @property useHashKey [io.github.devngho.kisopenapi.requests.auth.HashKey]를 사용해 요청을 보낼지 여부를 나타내는 플래그입니다.
 */
class KISApiClient internal constructor(
    val appKey: String,
    val appSecret: String,
    val isDemo: Boolean,
    /** 계좌번호(XXXXXXXX-XX 형식) */
    var account: List<String>?,
    var htsId: String? = null,
    var corp: CorporationRequest? = CorporationRequest(),
    var useHashKey: Boolean = false,
    var autoReconnect: Boolean = true,
    val rateLimiter: RateLimiter
) {
    lateinit var oauthToken: String
    var webSocketToken: String? = null

    /**
     * 웹소켓 세션의 CoroutineScope입니다.
     * @see io.github.devngho.kisopenapi.requests.LiveRequest
     */
    var webSocketScope: CoroutineScope? = null
    private var webSocketSession: WebSocketSession? = null

    /**
     * 웹소켓으로 들어오는 데이터 Flow입니다.
     * @see io.github.devngho.kisopenapi.requests.LiveRequest
     */
    var webSocketIncoming: SharedFlow<String>? = null

    /**
     * 웹소켓으로 나갈 데이터 Channel입니다.
     * @see io.github.devngho.kisopenapi.requests.LiveRequest
     */
    var webSocketOutgoing: SendChannel<String>? = null

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
    private val webSocketSubscribedInit = mutableMapOf<String, Pair<Mutex, LiveResponse?>>()
    private val webSocketSubscribedInitMutex = Mutex()


    /**
     * 구독 중인 요청 목록입니다.
     * 구독 상태를 복원하기 위해 사용합니다.
     * @see io.github.devngho.kisopenapi.requests.LiveRequest
     */
    private val webSocketSubscribed = mutableListOf<WebSocketSubscribed>()
    private val webSocketSubscribedMutex = Mutex()

    internal data class WebSocketSubscribed(
        val request: LiveRequest<out LiveData, out Response>,
        val data: LiveData,
        val initFunc: (suspend (Result<LiveResponse>) -> Unit)? = null,
        val block: suspend (Response) -> Unit
    )

    /**
     * 요청을 구독중으로 처리합니다.
     * @param request 구독할 요청
     * @param onAlreadyDone 이미 구독 중인 경우 실행할 함수
     * @return 구독 요청을 전송해야 하는지 여부
     */
    internal suspend fun subscribeStart(
        request: WebSocketSubscribed,
        onAlreadyDone: suspend (LiveResponse) -> Unit
    ): Boolean {
        val trKey = request.data.tradeKey(this)

        webSocketSubscribedMutex.withLock {
            webSocketSubscribed.add(request)
        }
        webSocketSubscribedKeyMutex.withLock {
            webSocketSubscribedKey.add(trKey)

            // 다른 곳에서 이미 구독 중인 경우, 그 구독 완료 정보를 가져옵니다.
            // 아직 구독 완료 정보가 없는 경우, 구독 완료 정보가 들어올 때까지 대기합니다.
            if (webSocketSubscribedKey.count { c -> c == trKey } > 1) {
                webSocketSubscribedInitMutex.withLock { webSocketSubscribedInit[trKey] }
                    ?.let {
                        it.first.withLock {
                            onAlreadyDone(webSocketSubscribedInit[trKey]!!.second!!)
                        }
                    }
                return false
            }
        }

        webSocketSubscribedInitMutex.withLock {
            // 구독 완료 정보를 대기하기 위한 뮤텍스를 생성합니다.
            val mtx = Mutex().also { it.lock() }
            webSocketSubscribedInit[trKey] = mtx to null
        }

        return true
    }

    /**
     * 요청을 구독 완료 처리합니다.
     * @param request 구독 완료할 요청
     * @param response 구독 완료 응답
     */
    internal suspend fun subscribeDone(request: WebSocketSubscribed, response: LiveResponse) {
        webSocketSubscribedInitMutex.withLock {
            webSocketSubscribedInit[request.data.tradeKey(this)]
                ?.let {
                    // 구독 완료 정보를 저장합니다.
                    webSocketSubscribedInit[request.data.tradeKey(this)] = it.copy(second = response)
                    it.first.unlock()
                }
        }
    }

    /**
     * 요청을 구독 해제 처리합니다.
     * @param request 구독 해제할 요청
     * @return 구독 해제 요청을 전송해야 하는지 여부
     * @see io.github.devngho.kisopenapi.requests.LiveRequest
     */
    internal suspend fun unsubscribe(request: WebSocketSubscribed): Boolean {
        webSocketSubscribedMutex.withLock {
            webSocketSubscribed.remove(request)
        }
        val trKey = request.data.tradeKey(this)
        return webSocketSubscribedKeyMutex.withLock {
            webSocketSubscribedKey.remove(trKey)
            !webSocketSubscribedKey.contains(trKey) // 다른 곳에서 구독 중인지 확인하고, 없으면 구독 해제 요청을 전송해야 함
        }
    }

    internal val httpClient = createHttpClient()

    sealed interface Event {
        data class OnOpen(val session: DefaultClientWebSocketSession) : Event
        data class OnSend(val message: String) : Event
        data class OnClose(val exception: Exception, val reason: CloseReason?) : Event
        data class OnError(val cause: Throwable) : Event
    }

    private val _eventFlow: MutableSharedFlow<Event> = MutableSharedFlow()
    val webSocketEventFlow: SharedFlow<Event> = _eventFlow.asSharedFlow()

    private var isClosing = false
    private val isClosingMutex = Mutex()

    suspend fun buildWebsocket(): DefaultClientWebSocketSession =
        httpClient.webSocketSession(if (isDemo) "ws://ops.koreainvestment.com:31000" else "ws://ops.koreainvestment.com:21000")
            .apply {
                setupVaraibles()
                setupSession()

                _eventFlow.emit(Event.OnOpen(this))
            }

    private fun DefaultClientWebSocketSession.setupVaraibles() {
        launch {
            isClosingMutex.withLock {
                isClosing = false
            }
        }

        webSocketIncoming = MutableSharedFlow(1)
        webSocketOutgoing = Channel(8)
        webSocketScope = this
        webSocketSession = this@setupVaraibles
    }

    private fun DefaultClientWebSocketSession.setupSession() {
        launch {
            for (it in (webSocketOutgoing!! as Channel<String>)) {
                _eventFlow.emit(Event.OnSend(it))
                send(Frame.Text(it))
            }
        }

        launch {
            try {
                for (it in incoming) processIncoming(it)
            } catch (e: Exception) {
                val cause = closeReason.await()

                withContext(NonCancellable) {
                    _eventFlow.emit(Event.OnClose(e, cause))
                }

                throw e
            } finally {
                if (autoReconnect) reconnectWebsocket()
                else if (!isClosing) closeWebsocket()
            }
        }
    }

    private suspend fun WebSocketSession.processIncoming(frame: Frame) {
        if (frame is Frame.Text) {
            try {
                val txt = frame.readText()
                (webSocketIncoming as MutableSharedFlow<String>).tryEmit(txt)
                processPingPong(frame, txt)
            } catch (e: Exception) {
                _eventFlow.tryEmit(Event.OnError(e))
            }
        }
    }

    private suspend fun WebSocketSession.processPingPong(frame: Frame, txt: String) {
        if (txt[0] == '0' || txt[0] == '1') return
        val resp = json.decodeFromString<LiveResponse>(txt)
        if (resp.header?.tradeId != "PINGPONG") return

        send(Frame.Pong(frame.data))
    }

    private fun clearSubscriptions() {
        if (webSocketSubscribedKeyMutex.isLocked) webSocketSubscribedKeyMutex.unlock()
        webSocketSubscribedKey.clear()

        if (webSocketSubscribedInitMutex.isLocked) webSocketSubscribedInitMutex.unlock()
        webSocketSubscribedInit.clear()

        if (webSocketSubscribedMutex.isLocked) webSocketSubscribedMutex.unlock()
        webSocketSubscribed.clear()
    }

    /**
     * 웹소켓을 종료합니다.
     */
    suspend fun closeWebsocket() {
        isClosingMutex.withLock {
            isClosing = true
        }

        clearSubscriptions()

        webSocketSession?.close()
        webSocketSession = null
        webSocketScope = null
        webSocketIncoming = null
    }

    /**
     * 웹소켓 연결을 다시 시도합니다.
     */
    @Suppress("unchecked_cast")
    suspend fun reconnectWebsocket() = coroutineScope {
        val copiedSubscriptions = webSocketSubscribedMutex.withLock { webSocketSubscribed.toList() }

        closeWebsocket()
        buildWebsocket()

        copiedSubscriptions.map {
            async {
                (it.request as LiveRequest<LiveData, *>).register(
                    it.data,
                    wait = true,
                    force = true,
                    it.initFunc,
                    it.block
                )
            }
        }.awaitAll()

        Unit
    }

    companion object {

        /**
         * KisOpenApi 객체를 생성합니다.
         * @param token 사용할 토큰
         * @param appKey 앱 키
         * @param appSecret 앱 시크릿
         * @param isDemo 모의투자 여부
         * @param websocketToken 웹소켓 토큰
         * @param account 계좌번호(XXXXXXXX-XX 형식)
         * @param id HTS ID
         * @param corp 호출하는 개인/기관 정보
         * @param hashKey 해시키 사용 여부
         * @param autoReconnect 웹소켓 연결이 끊겼을 때 자동으로 재연결할지 여부
         * @param rateLimiter 요청 속도 제한
         */
        @JvmStatic
        fun withToken(
            token: String,
            appKey: String,
            appSecret: String,
            isDemo: Boolean = false,
            websocketToken: String? = null,
            /** 계좌번호(XXXXXXXX-XX 형식) */
            account: String? = null,
            id: String? = null,
            corp: CorporationRequest? = CorporationRequest(),
            hashKey: Boolean = false,
            autoReconnect: Boolean = true,
            rateLimiter: RateLimiter? = null
        ) =
            KISApiClient(
                appKey,
                appSecret,
                isDemo,
                account?.split("-"),
                id,
                corp,
                hashKey,
                autoReconnect,
                rateLimiter ?: RateLimiter.defaultRate(isDemo)
            )
                .apply {
                    oauthToken = token
                    if (websocketToken != null) this.webSocketToken = websocketToken
                }

        /**
         * KisOpenApi 객체를 생성합니다.
         * 토큰 발급 API를 알아서 발급합니다.
         * @param appKey 앱 키
         *  @param appSecret 앱 시크릿
         *  @param isDemo 모의투자 여부
         *  @param grantWebsocket 웹소켓 토큰 발급 여부
         *  @param account 계좌번호(XXXXXXXX-XX 형식)
         *  @param id HTS ID
         *  @param corp 호출하는 개인/기관 정보
         *  @param hashKey 해시키 사용 여부
         *  @param autoReconnect 웹소켓 연결이 끊겼을 때 자동으로 재연결할지 여부
         *  @param rateLimiter 요청 속도 제한
         */
        @JvmStatic
        suspend fun with(
            appKey: String,
            appSecret: String,
            isDemo: Boolean = false,
            /** 계좌번호(XXXXXXXX-XX 형식) */
            account: String? = null,
            id: String? = null,
            corp: CorporationRequest? = CorporationRequest(),
            grantWebsocket: Boolean = false,
            hashKey: Boolean = false,
            autoReconnect: Boolean = true,
            rateLimiter: RateLimiter? = null
        ) =
            KISApiClient(
                appKey,
                appSecret,
                isDemo,
                account?.split("-"),
                id,
                corp,
                hashKey,
                autoReconnect,
                rateLimiter ?: RateLimiter.defaultRate(isDemo)
            )
                .apply {
                    oauthToken = GrantToken(this).call().getOrThrow().accessToken!!
                    if (grantWebsocket) this.webSocketToken = GrantLiveToken(this).call().getOrThrow().approvalKey
                }
    }
}