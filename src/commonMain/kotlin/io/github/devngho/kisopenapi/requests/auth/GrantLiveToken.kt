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
 * 웹소켓 연결을 위한 approval key를 발급받고, 반환합니다.
 */
class GrantLiveToken(override val client: KISApiClient) : NoDataRequest<GrantLiveToken.GrantTokenResponse> {
    @Serializable
    data class GrantTokenResponse(@SerialName("approval_key") val approvalKey: String?) : Response {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    data class GrantTokenData(
        @SerialName("grant_type")
        val grantType: String,
        @SerialName("appkey")
        val appKey: String,
        @SerialName("secretkey")
        val secretKey: String
    )

    override suspend fun call() = request {
        client.httpClient.post(
            "${client.options.baseUrl}/oauth2/Approval"
        ) {
            contentType(ContentType.Application.Json)
            setBody(GrantTokenData("client_credentials", client.appKey, client.appSecret))
        }
    }
}