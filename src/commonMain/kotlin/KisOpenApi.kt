package io.github.devngho.kisopenapi

import io.github.devngho.kisopenapi.requests.*
import io.github.devngho.kisopenapi.requests.response.CorporationRequest
import io.github.devngho.kisopenapi.requests.response.LiveResponse
import io.github.devngho.kisopenapi.requests.util.createHttpClient
import io.github.devngho.kisopenapi.requests.util.json
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import kotlin.jvm.JvmStatic

/**
 * 한국투자증권 API에 접근하기 위한 객체입니다.
 *
 * @property appKey 인증에 사용되는 애플리케이션 키입니다.
 * @property appSecret 인증에 사용되는 애플리케이션 서크릿입니다.
 * @property isDemo 데모 환경을 사용하는지 여부를 나타내는 플래그입니다.
 * @property account 계좌 번호입니다.
 * @property htsId HTS ID입니다.
 * @property corp 개인/법인 정보입니다.
 * @property useHashKey [io.github.devngho.kisopenapi.requests.HashKey]를 사용해 요청을 보낼지 여부를 나타내는 플래그입니다.
 */
class KisOpenApi internal constructor(
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
    var websocketToken: String? = null

    /**
     * 웹소켓 세션입니다.
     * @see io.github.devngho.kisopenapi.requests.LiveRequest
     */
    var websocket: DefaultClientWebSocketSession? = null

    /**
     * 웹소켓으로 들어오는 데이터 Flow입니다.
     * @see io.github.devngho.kisopenapi.requests.LiveRequest
     */
    var websocketIncoming: MutableSharedFlow<String>? = null

    /**
     * tr_key 목록입니다.
     * 구독을 해제할 때, 다른 곳에서 구독 중인지 확인하기 위해 사용합니다.
     * @see io.github.devngho.kisopenapi.requests.LiveRequest
     */
    private val websocketSubscribedKey = mutableListOf<String>()
    private val websocketSubscribedKeyMutex = Mutex()

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
        val initFunc: ((LiveResponse) -> Unit)? = null,
        val block: (Response) -> Unit
    )

    /**
     * 요청을 구독 처리합니다.
     * @param request 구독할 요청
     */
    internal suspend fun subscribe(request: WebSocketSubscribed) {
        webSocketSubscribedMutex.withLock {
            webSocketSubscribed.add(request)
        }
        websocketSubscribedKeyMutex.withLock {
            websocketSubscribedKey.add(request.data.tradeKey(this))
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
        websocketSubscribedKeyMutex.withLock {
            websocketSubscribedKey.remove(trKey)
            return websocketSubscribedKey.contains(trKey)
        }
    }

    internal val httpClient = createHttpClient()

    suspend fun buildWebsocket(): DefaultClientWebSocketSession {
        httpClient.webSocketSession(if (isDemo) "ws://ops.koreainvestment.com:31000" else "ws://ops.koreainvestment.com:21000").run {
            websocketIncoming = MutableSharedFlow()
            websocket = this

            if (autoReconnect) launch {
                closeReason.await()
                reconnectWebsocket()
            }

            launch {
                incoming.consumeEach {
                    if (it is Frame.Text) {
                        val txt = it.readText()
                        websocketIncoming?.emit(txt)

                        // PING-PONG
                        txt
                            .takeIf { v -> v[0] != '0' && v[0] != '1' }
                            ?.let { v -> json.decodeFromString<LiveResponse>(v) }
                            ?.apply {
                                if (this.header?.tradeId == "PINGPONG") send(it)
                            }
                    }
                }
            }

            return this
        }
    }

    /**
     * 웹소켓을 종료합니다.
     */
    suspend fun closeWebsocket() {
        websocket?.close()
        websocket = null
        websocketIncoming = null
    }

    /**
     * 웹소켓 연결을 다시 시도합니다.
     */
    @Suppress("unchecked_cast")
    suspend fun reconnectWebsocket() {
        closeWebsocket()
        buildWebsocket()

        // 연결 요청 다시 전송
        webSocketSubscribedMutex.withLock {
            webSocketSubscribed.forEach {
                (it.request as LiveRequest<LiveData, *>).register(it.data, it.initFunc, it.block)
            }
        }
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
            KisOpenApi(
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
                    if(websocketToken != null) this.websocketToken = websocketToken
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
            KisOpenApi(
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
                    oauthToken = GrantToken(this).call().accessToken!!
                    if(grantWebsocket) this.websocketToken = GrantLiveToken(this).call().approvalKey
                }
    }
}