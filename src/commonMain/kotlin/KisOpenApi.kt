package io.github.devngho.kisopenapi

import io.github.devngho.kisopenapi.requests.GrantLiveToken
import io.github.devngho.kisopenapi.requests.GrantToken
import io.github.devngho.kisopenapi.requests.response.CorporationRequest
import io.github.devngho.kisopenapi.requests.response.LiveResponse
import io.github.devngho.kisopenapi.requests.util.createHttpClient
import io.github.devngho.kisopenapi.requests.util.json
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlin.jvm.JvmStatic

class KisOpenApi internal constructor(val appKey: String, val appSecret: String, val isDemo: Boolean, /** 계좌번호(XXXXXXXX-XX 형식) */var account: List<String>?, var htsId: String? = null, var corp: CorporationRequest? = CorporationRequest(), var useHashKey: Boolean = false) {
    lateinit var oauthToken: String
    var websocketToken: String? = null
    var websocket: DefaultClientWebSocketSession? = null
    var websocketIncoming: MutableSharedFlow<Frame>? = null


    val httpClient = createHttpClient()

    suspend fun buildWebsocket(): DefaultClientWebSocketSession {
        httpClient.webSocketSession(if (isDemo) "ws://ops.koreainvestment.com:31000" else "ws://ops.koreainvestment.com:21000").run {
            websocketIncoming = MutableSharedFlow()
            websocket = this

            launch {
                incoming.consumeEach {
                    websocketIncoming?.emit(it)
                    if (it is Frame.Text)
                        it.readText()
                            .apply { if (this[0] == '0' || this[0] == '1') return@consumeEach }
                            .apply {
                                json.decodeFromString<LiveResponse>(this).run {
                                    if (this.header?.tradeId == "PINGPONG") send(this@apply)
                                }
                            }
                }
            }

            return this
        }
    }

    companion object {

        /**
         * KisOpenApi 객체를 생성합니다.
         */
        @JvmStatic
        fun withToken(token: String, appKey: String, appSecret: String, isDemo: Boolean = false,  websocketToken: String? = null,/** 계좌번호(XXXXXXXX-XX 형식) */account: String? = null, id: String? = null, corp: CorporationRequest? = CorporationRequest(), hashKey: Boolean = false) =
            KisOpenApi(appKey, appSecret, isDemo, account?.split("-"), id, corp, hashKey)
                .apply {
                    oauthToken = token
                    if(websocketToken != null) this.websocketToken = websocketToken
                }

        /**
         * KisOpenApi 객체를 생성합니다.
         * 토큰 발급 API를 알아서 발급합니다.
         */
        @JvmStatic
        suspend fun with(appKey: String, appSecret: String, isDemo: Boolean = false,  /** 계좌번호(XXXXXXXX-XX 형식) */account: String? = null, id: String? = null, corp: CorporationRequest? = CorporationRequest(), grantWebsocket: Boolean = false, hashKey: Boolean = false) =
            KisOpenApi(appKey, appSecret, isDemo, account?.split("-"), id, corp, hashKey)
                .apply {
                    oauthToken = GrantToken(this).call().accessToken
                    if(grantWebsocket) this.websocketToken = GrantLiveToken(this).call().approvalKey
                }
    }
}