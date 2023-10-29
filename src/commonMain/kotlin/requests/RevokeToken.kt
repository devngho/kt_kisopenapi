package io.github.devngho.kisopenapi.requests

import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.response.CorporationRequest
import io.github.devngho.kisopenapi.requests.util.RequestError
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("unused")
class RevokeToken(override val client: KisOpenApi):
    DataRequest<RevokeToken.RevokeTokenData, RevokeToken.RevokeTokenResponse> {
    @Serializable
    data class RevokeTokenResponse(val code: Int?, val message: String?): Response {
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

    data class RevokeTokenData(val token: String, override val corp: CorporationRequest? = null): Data

    override suspend fun call(data: RevokeTokenData): RevokeTokenResponse = client.rateLimiter.rated {

        client.httpClient.post(
            if (client.isDemo) "https://openapi.koreainvestment.com:9443/oauth2/revokeP"
            else               "https://openapivts.koreainvestment.com:29443/oauth2/revokeP"
        ) {
            contentType(ContentType.Application.Json)
            setBody(RevokeTokenJson(data.token, client.appKey, client.appSecret))
        }.body<RevokeTokenResponse>().run {
            if (this.errorCode != null) throw RequestError(this.errorDescription)
            this
        }
    }
}