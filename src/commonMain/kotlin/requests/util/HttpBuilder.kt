package io.github.devngho.kisopenapi.requests.util

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.serialization.kotlinx.bigdecimal.DecimalModeSerializer
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

@OptIn(ExperimentalSerializationApi::class)
val json = Json {
    explicitNulls = false
    serializersModule = SerializersModule {
        contextual(BigInteger::class, BigIntegerPreciseSerializer)
        contextual(BigDecimal::class, BigDecimalPreciseSerializer)
        contextual(DecimalMode::class, DecimalModeSerializer)
    }
    ignoreUnknownKeys = true
}

fun createHttpClient() = HttpClient {
    install(ContentNegotiation) {
        json(json)
    }
    install(WebSockets)
}