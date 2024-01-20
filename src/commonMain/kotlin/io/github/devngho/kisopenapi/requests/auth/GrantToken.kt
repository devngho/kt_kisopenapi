package io.github.devngho.kisopenapi.requests.auth

import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.NoDataRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.util.request
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * API를 사용하기 위한 access token을 발급받고, 반환합니다.
 */
class GrantToken(override val client: KISApiClient) : NoDataRequest<GrantToken.GrantTokenResponse> {
    @Serializable
    data class GrantTokenResponse(
        @SerialName("access_token") val accessToken: String?,
        @SerialName("expires_in") val expiresIn: Int?
    ) : Response {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class GrantTokenJson(
        @SerialName("grant_type") val grantType: String,
        @SerialName("appkey") val appKey: String,
        @SerialName("appsecret") val appSecret: String
    )

    override suspend fun call() = request {
        client.httpClient.post(
            "${client.options.baseUrl}/oauth2/tokenP"
        ) {
            contentType(ContentType.Application.Json)
            setBody(GrantTokenJson("client_credentials", client.appKey, client.appSecret))
        }
    }
}