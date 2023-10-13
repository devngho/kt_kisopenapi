package io.github.devngho.kisopenapi.requests

import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.response.CorporationRequest
import io.github.devngho.kisopenapi.requests.util.RequestError
import io.github.devngho.kisopenapi.requests.util.json
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

class HashKey(override val client: KisOpenApi): DataRequest<HashKey.HashKeyData, HashKey.HashKeyResponse> {
    @Serializable
    data class HashKeyResponse(@SerialName("HASH") val hash: String): Response {
        override val errorDescription: String? = null
        override val errorCode: String? = null
    }

    data class HashKeyData(val value: String): Data {
        override val corp: CorporationRequest? = null
    }

    override suspend fun call(data: HashKeyData): HashKeyResponse {
        return client.httpClient.post(
            if (client.isDemo) "https://openapivts.koreainvestment.com:29443/uapi/hashkey"
            else               "https://openapi.koreainvestment.com:9443/uapi/hashkey"
        ) {
            contentType(ContentType.Application.Json)
            setBody(data.value)
        }.body<HashKeyResponse>().run {
            if (this.errorCode != null) throw RequestError(this.errorDescription)
            this
        }
    }

    companion object {
        @Suppress("SpellCheckingInspection")
        suspend inline fun <reified T> HttpRequestBuilder.hashKey(client: KisOpenApi) = client.rateLimiter.rated {
            if (client.useHashKey) {
                val data = HashKeyData(json.encodeToString(this.body as T))
                val hash = HashKey(client).call(data)
                headers {
                    append("hashkey", hash.hash)
                }
            }
        }
    }
}