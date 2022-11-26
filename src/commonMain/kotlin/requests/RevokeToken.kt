package io.github.devngho.kisopenapi.requests

import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.response.CorporationRequest
import io.github.devngho.kisopenapi.requests.util.RequestError
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

class RevokeToken(override val client: KisOpenApi):
    DataRequest<RevokeToken.RevokeTokenData, RevokeToken.RevokeTokenResponse> {
    @Serializable
    data class RevokeTokenResponse(val code: Int, val message: String): Response {
        override val error_description: String? = null
        override val error_code: String? = null
    }

    @Serializable
    data class RevokeTokenJson(val token: String, val appkey: String, val appsecret: String)

    data class RevokeTokenData(val token: String, override val corp: CorporationRequest? = null): Data

    override suspend fun call(data: RevokeTokenData): RevokeTokenResponse {
        return client.httpClient.post(
            if (client.isDemo) "https://openapi.koreainvestment.com:9443/oauth2/revokeP"
            else               "https://openapivts.koreainvestment.com:29443/oauth2/revokeP"
        ) {
            contentType(ContentType.Application.Json)
            setBody(RevokeTokenJson(data.token, client.appKey, client.appSecret))
        }.body<RevokeTokenResponse>().run {
            if (this.error_code != null) throw RequestError(this.error_description)
            this
        }
    }
}