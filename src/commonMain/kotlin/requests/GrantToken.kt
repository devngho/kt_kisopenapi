package io.github.devngho.kisopenapi.requests

import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.util.RequestError
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

class GrantToken(override val client: KisOpenApi): NoDataRequest<GrantToken.GrantTokenResponse> {
    @Serializable
    data class GrantTokenResponse(val access_token: String, val expires_in: Int): Response {
        override val error_description: String? = null
        override val error_code: String? = null
    }

    @Serializable
    data class GrantTokenJson(val grant_type: String, val appkey: String, val appsecret: String)

    override suspend fun call(): GrantTokenResponse {
        return client.httpClient.post(
            if (client.isDemo) "https://openapi.koreainvestment.com:9443/oauth2/tokenP"
            else               "https://openapivts.koreainvestment.com:29443/oauth2/tokenP"
        ) {
            contentType(ContentType.Application.Json)
            setBody(GrantTokenJson("client_credentials", client.appKey, client.appSecret))
        }.body<GrantTokenResponse>().run {
            if (this.error_code != null) throw RequestError(this.error_description)
            this
        }
    }
}