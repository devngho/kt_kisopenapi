package io.github.devngho.kisopenapi.requests

import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.util.RequestError
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class GrantLiveToken(override val client: KisOpenApi): NoDataRequest<GrantLiveToken.GrantTokenResponse> {
    @Serializable
    data class GrantTokenResponse(@SerialName("approval_key") val approvalKey: String): Response {
        override val errorDescription: String? = null
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class GrantTokenData(
        @SerialName("grant_type")
        val grantType: String,
        @SerialName("appkey")
        val appKey: String,
        @SerialName("secretkey")
        val secretKey: String
    )

    override suspend fun call(): GrantTokenResponse = client.rateLimiter.rated {
        client.httpClient.post(
            if (client.isDemo) "https://openapivts.koreainvestment.com:29443/oauth2/Approval"
            else               "https://openapi.koreainvestment.com:9443/oauth2/Approval"
        ) {
            contentType(ContentType.Application.Json)
            setBody(GrantTokenData("client_credentials", client.appKey, client.appSecret))
        }.body<GrantTokenResponse>().run {
            if (this.errorCode != null) throw RequestError(this.errorDescription)
            this
        }
    }
}