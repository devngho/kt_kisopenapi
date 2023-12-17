package io.github.devngho.kisopenapi.requests.auth

import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Data
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.util.Result
import io.github.devngho.kisopenapi.requests.util.request
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("unused")
class RevokeToken(override val client: KISApiClient) :
    DataRequest<RevokeToken.RevokeTokenData, RevokeToken.RevokeTokenResponse> {
    @Serializable
    data class RevokeTokenResponse(val code: Int?, val message: String?) : Response {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class RevokeTokenJson(
        val token: String,
        @SerialName("appkey") val appKey: String,
        @SerialName("appsecret") val appSecret: String
    )

    data class RevokeTokenData(val token: String, override var corp: CorporationRequest? = null) : Data

    override suspend fun call(data: RevokeTokenData): Result<RevokeTokenResponse> = request(data) {
        client.httpClient.post(
            if (client.isDemo) "https://openapi.koreainvestment.com:9443/oauth2/revokeP"
            else "https://openapivts.koreainvestment.com:29443/oauth2/revokeP"
        ) {
            contentType(ContentType.Application.Json)
            setBody(RevokeTokenJson(it.token, client.appKey, client.appSecret))
        }
    }
}