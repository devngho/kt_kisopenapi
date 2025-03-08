package io.github.devngho.kisopenapi

import io.github.devngho.kisopenapi.KISApiClient.Companion.options
import io.github.devngho.kisopenapi.requests.auth.GrantLiveToken
import io.github.devngho.kisopenapi.requests.auth.GrantToken
import io.github.devngho.kisopenapi.requests.auth.RevokeToken
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.ratelimit.RateLimiter
import io.github.devngho.kisopenapi.requests.response.LiveResponse
import io.github.devngho.kisopenapi.requests.util.InternalApi
import io.github.devngho.kisopenapi.requests.util.WebSocketSubscribed
import io.github.devngho.kisopenapi.requests.util.createHttpClient
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.jvm.JvmStatic

/**
 * 한국투자증권 API에 접근하기 위한 객체입니다.
 *
 * @property appKey 인증에 사용되는 애플리케이션 키입니다.
 * @property appSecret 인증에 사용되는 애플리케이션 서크릿입니다.
 * @property isDemo 모의 투자 환경을 사용하는지 여부를 나타내는 플래그입니다.
 * @property account 계좌 번호입니다.
 * @property htsId HTS ID입니다.
 * @property corpRequest 개인/법인 정보입니다.
 * @property tokens API 토큰입니다.
 * @property options API 옵션입니다.
 */
@OptIn(InternalApi::class)
interface KISApiClient {
    val appKey: String
    val appSecret: String
    val isDemo: Boolean

    /**
     * 계좌번호입니다. first는 계좌번호, second는 계좌 상품입니다.
     */
    val account: Pair<String, String>?

    /**
     * HTS ID입니다.
     */
    val htsId: String?

    /**
     * 법인, 개인 등 호출하는 개인/기관 정보입니다.
     */
    val corpRequest: CorporationRequest?

    /**
     * API 요청 옵션입니다.
     */
    val options: KISApiOptions

    /**
     * API 요청에 사용할 토큰입니다.
     */
    val tokens: KISApiTokens

    /**
     * API 요청을 전송할 클라이언트입니다.
     */
    val httpClient: HttpClient

    /**
     * 웹소켓 연결에 사용할 클라이언트입니다.
     */
    val webSocket: WebSocket

    /**
     * 웹소켓 구독 요청을 관리하는 [WebSocketManager]입니다.
     */
    val webSocketManager: WebSocketManager

    /**
     * API 옵션입니다.
     */
    @Serializable
    data class KISApiOptions(
        /**
         * 요청을 전송할 때, 해시키 API를 사용하려면 true, 아니면 false로 설정하세요. 기본값은 false입니다.
         */
        var useHashKey: Boolean = false,
        /**
         * 웹소켓이 비정상적으로 종료되었을 때, 자동으로 재연결하려면 true, 아니면 false로 설정하세요. 기본값은 true입니다.
         */
        var autoReconnect: Boolean = true,
        /**
         * 요청을 전송할 때 초당 호출 유량 제한을 따르기 위한 [RateLimiter]입니다.
         */
        var rateLimiter: RateLimiter,
        /**
         * 웹소켓 연결에 사용할 클라이언트입니다.
         */
        var webSocketClient: WebSocket,
        /**
         * 웹소켓 요청을 관리하는 [WebSocketManager]입니다.
         */
        var webSocketManager: WebSocketManager,
        /**
         * API를 호출할 접속 URL입니다.
         */
        var baseUrl: String,
        /**
         * 웹소켓 접속 URL입니다.
         */
        var webSocketUrl: String,
        /**
         * 웹소켓 전송 타임아웃입니다. 단위는 초입니다. 기본값은 10초입니다.
         *
         * **구현 상세**
         *
         * 전송 타임아웃이 지나는 동안 전송이 완료되지 않으면 웹소켓을 종료합니다.
         */
        var webSocketSendTimeout: Long = 10,
        /**
         * 웹소켓 수신 타임아웃입니다. 단위는 초입니다. 기본값은 20초입니다.
         *
         * **구현 상세**
         *
         * 수신 타임아웃이 지나는 동안 수신된 데이터가 없으면 웹소켓을 종료합니다.
         * 기본적으로 PINGPONG 메시지를 주고받으므로, 일반적으로는 수신 타임아웃이 지나는 일은 없습니다.
         */
        var webSocketReceiveTimeout: Long = 20,
    )

    /**
     * API 요청에 사용할 토큰입니다.
     */
    @Serializable
    data class KISApiTokens(
        /**
         * API 요청에 사용할 토큰입니다.
         *
         * @see io.github.devngho.kisopenapi.requests.auth.GrantToken
         */
        var oauthToken: String? = null,
        /**
         * 웹소켓 요청에 사용할 토큰입니다.
         *
         * @see io.github.devngho.kisopenapi.requests.auth.GrantLiveToken
         */
        var webSocketToken: String? = null,
        /**
         * 웹소켓 토큰이 만료되는 시간의 epoch seconds입니다.
         */
        var webSocketTokenExpire: Long? = null,
        /**
         * 토큰을 발급받을 때 사용할 [KISApiClient]입니다.
         */
        @Transient
        var client: KISApiClient? = null,
    ) {
        suspend fun revoke() {
            if (client != null && oauthToken != null) RevokeToken(client!!).call(RevokeToken.RevokeTokenData(oauthToken!!))

            oauthToken = null
            webSocketToken = null
            webSocketTokenExpire = null
        }

        suspend fun issue() {
            client?.let {
                GrantToken(it).call().getOrThrow().let { resp ->
                    oauthToken = resp.accessToken
                    webSocketTokenExpire = Clock.System.now().epochSeconds + resp.expiresIn!!
                }
                webSocketToken = GrantLiveToken(it).call().getOrThrow().approvalKey
            }
        }

        suspend fun issueIfExpired() {
            if (isExpired) issue()
        }

        /**
         * 웹소켓 토큰이 만료되었는지 확인하고, 만료되었다면 true, 아니면 false를 반환합니다.
         * 만약 토큰이 존재하지 않으면 true를 반환합니다.
         */
        val isExpired: Boolean
            get() {
                if (this.oauthToken == null || this.webSocketToken == null || webSocketTokenExpire == null) return true

                val now = Clock.System.now().epochSeconds

                return now >= webSocketTokenExpire!!
            }
    }

    /**
     * 웺소켓 구독 요청을 관리하는 [WebSocketManager]입니다.
     */
    @InternalApi
    interface WebSocketManager {
        /**
         * 요청을 구독하는 중으로 표시합니다.
         *
         * @param request 구독할 요청
         * @param force 구독 중인 요청이 있어도 강제로 구독하려면 true, 아니면 false
         * @param onAlreadyDone 이미 구독 중인 경우 실행할 함수
         * @return 구독 요청을 전송할 필요가 있으면 true, 아니면 false
         */
        suspend fun subscribeStart(
            request: WebSocketSubscribed,
            force: Boolean = false,
            onAlreadyDone: suspend (LiveResponse) -> Unit
        ): Boolean

        /**
         * 요청을 구독 완료 처리합니다.
         *
         * @param request 구독 완료할 요청
         * @param response 구독 완료 응답
         */
        suspend fun subscribeDone(request: WebSocketSubscribed, response: LiveResponse)

        /**
         * 요청을 구독 해제 처리합니다.
         *
         * @param request 구독 해제할 요청
         * @return 구독 해제 요청을 전송할 필요가 있으면 true, 아니면 false
         * @see io.github.devngho.kisopenapi.requests.LiveRequest
         */
        suspend fun unsubscribe(request: WebSocketSubscribed): Boolean

        /**
         * 등록된 요청을 모두 해제합니다. 실제로 해제 요청을 전송하지 않습니다.
         */
        suspend fun clearSubscribed()

        /**
         * 구독 중인 요청 정보를 모두 반환합니다.
         */
        fun getSubscribed(): List<WebSocketSubscribed>
    }

    /**
     * 웹소켓 클라이언트입니다.
     */
    interface WebSocket {
        /**
         * 웹소켓에서 발생한 이벤트를 나타냅니다.
         */
        sealed interface Event {
            /**
             * 웹소켓이 연결되었을 때 발생합니다.
             *
             * @param session 연결된 세션
             */
            data class OnOpen(val session: DefaultClientWebSocketSession) : Event

            /**
             * 웹소켓으로 데이터를 전송할 때 발생합니다.
             *
             * @param message 전송할 데이터
             */
            data class OnSend(val message: String) : Event

            /**
             * 웹소켓이 닫혔을 때 발생합니다.
             *
             * @param reason 닫힌 이유
             * @param exception 예외
             */
            data class OnClose(val reason: CloseReason?, val exception: Throwable?) : Event

            /**
             * 웹소켓에서 알 수 없는 에러가 발생했을 때 발생합니다.
             */
            data class OnError(val cause: Throwable) : Event
        }

        /**
         * 웹소켓으로 들어오는 데이터 Flow입니다.
         * @see io.github.devngho.kisopenapi.requests.LiveRequest
         */
        val incoming: SharedFlow<String>

        /**
         * 웹소켓으로 나갈 데이터 Channel입니다.
         * @see io.github.devngho.kisopenapi.requests.LiveRequest
         */
        val outgoing: SendChannel<String>

        /**
         * 웹소켓에서 발생한 [Event] Flow입니다.
         */
        val eventFlow: SharedFlow<Event>

        /**
         * 웹소켓 세션의 [CoroutineScope]입니다.
         */
        var scope: CoroutineScope?

        /**
         * 웹소켓이 연결되었으면 true, 아니면 false입니다.
         */
        val isConnected: Boolean

        /**
         * 웹소켓 세션을 종료합니다.
         */
        suspend fun closeWebsocket()

        /**
         * 웹소켓 연결을 종료한 후 다시 연결합니다.
         */
        suspend fun reconnectWebsocket()

        /**
         * 웹소켓 세션을 생성합니다.
         * 기존 세션이 있더라도 새로운 세션을 생성합니다.
         */
        suspend fun buildWebsocket()
    }

    companion object {
        /**
         * KISApiClient 객체를 생성해 반환합니다.
         *
         * @param tokens 사용할 토큰
         * @param appKey 앱 키
         * @param appSecret 앱 시크릿
         * @param isDemo 모의투자 여부
         * @param account 계좌번호(XXXXXXXX-XX 형식)
         * @param id HTS ID
         * @param corp 호출하는 개인/기관 정보
         * @param httpClient HTTP 클라이언트. null이면 기본 클라이언트를 사용합니다.
         * @param options 요청 옵션
         */
        @JvmStatic
        fun withToken(
            tokens: KISApiTokens,
            appKey: String,
            appSecret: String,
            isDemo: Boolean = false,
            account: String? = null,
            id: String? = null,
            corp: CorporationRequest? = CorporationRequest(),
            httpClient: HttpClient? = null,
            options: KISApiOptions.(KISApiClient) -> Unit = { },
        ): KISApiClient =
            KISApiClientImpl(
                httpClient ?: createHttpClient(),
                appKey,
                appSecret,
                isDemo,
                account?.split("-")?.let { Pair(it[0], it[1]) },
                id,
                corp,
                tokens
            ).apply {
                this.tokens.client = this

                this.options(options)
            }

        /**
         * KISApiClient 객체를 생성해 반환합니다.
         * 토큰 발급 API를 알아서 호출해 토큰을 발급합니다.
         *
         * @param appKey 앱 키
         *  @param appSecret 앱 시크릿
         *  @param isDemo 모의투자 여부
         *  @param account 계좌번호(XXXXXXXX-XX 형식)
         *  @param id HTS ID
         *  @param corp 호출하는 개인/기관 정보
         *  @param httpClient HTTP 클라이언트. null이면 기본 클라이언트를 사용합니다.
         *  @param options 요청 옵션
         */
        @JvmStatic
        suspend fun with(
            appKey: String,
            appSecret: String,
            isDemo: Boolean = false,
            account: String? = null,
            id: String? = null,
            corp: CorporationRequest? = CorporationRequest(),
            httpClient: HttpClient? = null,
            options: KISApiOptions.(KISApiClient) -> Unit = { },
        ): KISApiClient =
            KISApiClientImpl(
                httpClient ?: createHttpClient(),
                appKey,
                appSecret,
                isDemo,
                account?.split("-")?.let { Pair(it[0], it[1]) },
                id,
                corp,
                KISApiTokens()
            )
                .apply {
                    tokens.client = this

                    tokens.issue()

                    this.options(options)
                }

        /**
         * KISApiClient 옵션을 설정하고 KISApiClient 객체를 반환합니다.
         */
        fun KISApiClient.options(block: KISApiOptions.(KISApiClient) -> Unit) =
            this.apply { options.apply { block(this@options) } }
    }
}