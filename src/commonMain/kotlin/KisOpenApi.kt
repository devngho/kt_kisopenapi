package io.github.devngho.kisopenapi

import io.github.devngho.kisopenapi.requests.GrantLiveToken
import io.github.devngho.kisopenapi.requests.GrantToken
import io.github.devngho.kisopenapi.requests.response.CorporationRequest
import io.github.devngho.kisopenapi.requests.util.createHttpClient

class KisOpenApi internal constructor(val appKey: String, val appSecret: String, val isDemo: Boolean, /** 계좌번호(XXXXXXXX-XX 형식) */var account: List<String>?, var corp: CorporationRequest? = CorporationRequest()) {
    lateinit var oauthToken: String
    lateinit var websocketToken: String

    val httpClient = createHttpClient()

    companion object {

        /**
         * KisOpenApi 객체를 생성합니다.
         */
        fun withToken(token: String, appKey: String, appSecret: String, isDemo: Boolean = false,  websocketToken: String? = null,/** 계좌번호(XXXXXXXX-XX 형식) */account: String? = null, corp: CorporationRequest? = null) =
            KisOpenApi(appKey, appSecret, isDemo, account?.split("-"), corp).apply { oauthToken = token; if(websocketToken != null) this.websocketToken = websocketToken }

        /**
         * KisOpenApi 객체를 생성합니다.
         * 토큰 발급 API를 알아서 발급합니다.
         */
        suspend fun with(appKey: String, appSecret: String, isDemo: Boolean = false,  /** 계좌번호(XXXXXXXX-XX 형식) */account: String? = null, corp: CorporationRequest? = null, grantWebsocket: Boolean = false) =
            KisOpenApi(appKey, appSecret, isDemo, account?.split("-"), corp).apply { oauthToken = GrantToken(this).call().access_token; if(grantWebsocket) this.websocketToken = GrantLiveToken(this).call().approval_key }
    }
}