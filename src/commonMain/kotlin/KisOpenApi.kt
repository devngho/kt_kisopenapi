package com.github.devngho.kisopenapi

import com.github.devngho.kisopenapi.requests.EmptyData
import com.github.devngho.kisopenapi.requests.GrantToken
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

class KisOpenApi internal constructor(val appKey: String, val appSecret: String, val isDemo: Boolean) {

    lateinit var oauthToken: String
    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    companion object {
        /**
         * KisOpenApi 객체를 생성합니다.
         */
        fun withToken(token: String, appKey: String, appSecret: String, isDemo: Boolean) =
            KisOpenApi(appKey, appSecret, isDemo).apply { oauthToken = token }

        /**
         * KisOpenApi 객체를 생성합니다.
         * 토큰 발급 API를 알아서 발급합니다.
         */
        suspend fun with(appKey: String, appSecret: String, isDemo: Boolean) =
            KisOpenApi(appKey, appSecret, isDemo).apply { oauthToken = GrantToken(this).call(EmptyData).access_token }
    }
}