package com.github.devngho.kisopenapi.requests

import com.github.devngho.kisopenapi.KisOpenApi
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class GrantToken(override val client: KisOpenApi): DataRequest<EmptyData, GrantToken.GrantTokenResponse> {
    data class GrantTokenResponse(val access_token: String, val expires_in: Int): Response
    data class GrantTokenData(val grant_type: String, val appKey: String, val appSecret: String): Data

    override suspend fun call(data: EmptyData): GrantTokenResponse {
        return client.httpClient.post(
            if (client.isDemo) "https://openapi.koreainvestment.com:9443/oauth2/tokenP"
            else               "https://openapivts.koreainvestment.com:29443/oauth2/tokenP"
        ) {
            contentType(ContentType.Application.Json)
            setBody(GrantTokenData("client_credentials", client.appKey, client.appSecret))
        }.body()
    }
}