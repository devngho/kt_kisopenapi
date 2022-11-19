package io.github.devngho.kisopenapi.requests.util

import com.ionspin.kotlin.bignum.serialization.kotlinx.humanReadableSerializerModule
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
fun createHttpClient() = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            explicitNulls = true
            serializersModule = humanReadableSerializerModule
            ignoreUnknownKeys = true
        })
    }
}