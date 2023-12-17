package io.github.devngho.kisopenapi.requests.auth

import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Data
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.util.json
import io.github.devngho.kisopenapi.requests.util.request
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

class HashKey(override val client: KISApiClient) : DataRequest<HashKey.HashKeyData, HashKey.HashKeyResponse> {
    @Serializable
    data class HashKeyResponse(@SerialName("HASH") val hash: String) : Response {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    data class HashKeyData(val value: String) : Data {
        override var corp: CorporationRequest? = null
    }

    override suspend fun call(data: HashKeyData) = request(data) {
        client.httpClient.post(
            if (client.isDemo) "https://openapivts.koreainvestment.com:29443/uapi/hashkey"
            else "https://openapi.koreainvestment.com:9443/uapi/hashkey"
        ) {
            contentType(ContentType.Application.Json)
            setBody(data.value)
        }
    }

    companion object {
        @Suppress("SpellCheckingInspection")
        suspend inline fun <reified T> HttpRequestBuilder.hashKey(client: KISApiClient) {
            if (client.useHashKey) {
                val data = HashKeyData(json.encodeToString(this.body as T))
                val hash = HashKey(client).call(data).getOrThrow()
                headers {
                    append("hashkey", hash.hash)
                }
            }
        }
    }
}