package io.github.devngho.kisopenapi.requests.util

import com.ionspin.kotlin.bignum.serialization.kotlinx.humanReadableSerializerModule
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
actual fun createHttpClient(): HttpClient =
    HttpClient(CIO) {
        install(ContentNegotiation){
            json(Json {
                ignoreUnknownKeys = true
                serializersModule = humanReadableSerializerModule
                isLenient = true
                explicitNulls = false
            })
        }
    }