package io.github.devngho.kisopenapi.requests.util

import com.ionspin.kotlin.bignum.serialization.kotlinx.humanReadableSerializerModule
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

val json = Json {
    explicitNulls = false
    serializersModule = humanReadableSerializerModule
    ignoreUnknownKeys = true
}

@OptIn(ExperimentalSerializationApi::class)
fun createHttpClient() = HttpClient {
    install(ContentNegotiation) {
        json(json)
    }
    install(WebSockets)
}