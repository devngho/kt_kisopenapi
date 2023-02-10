package io.github.devngho.kisopenapi.requests

import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.util.RequestError
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class GrantLiveToken(override val client: KisOpenApi): NoDataRequest<GrantLiveToken.GrantTokenResponse> {
    @Serializable
    data class GrantTokenResponse(@SerialName("approval_key") val approvalKey: String): Response {
        override val error_description: String? = null
        override val error_code: String? = null
    }

    @Serializable
    data class GrantTokenJson(val grant_type: String, val appkey: String, val secretkey: String)

    override suspend fun call(): GrantTokenResponse {
        return client.httpClient.post(
            if (client.isDemo) "https://openapivts.koreainvestment.com:29443/oauth2/Approval"
            else               "https://openapi.koreainvestment.com:9443/oauth2/Approval"
        ) {
            contentType(ContentType.Application.Json)
            setBody(GrantTokenJson("client_credentials", client.appKey, client.appSecret))
        }.body<GrantTokenResponse>().run {
            if (this.error_code != null) throw RequestError(this.error_description)
            this
        }
    }
}