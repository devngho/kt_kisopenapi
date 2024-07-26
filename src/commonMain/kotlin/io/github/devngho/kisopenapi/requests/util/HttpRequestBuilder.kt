package io.github.devngho.kisopenapi.requests.util

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.serialization.kotlinx.bigdecimal.DecimalModeSerializer
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Data
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.NoDataRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.data.Msg
import io.github.devngho.kisopenapi.requests.data.TradeContinuousResponse
import io.github.devngho.kisopenapi.requests.data.TradeIdMsg
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
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
    encodeDefaults = true
}

internal fun createHttpClient() = HttpClient {
    install(ContentNegotiation) {
        json(json)
    }
    install(WebSockets)
}

internal fun HttpRequestBuilder.setAuth(client: KISApiClient) {
    contentType(ContentType.Application.Json)
    headers {
        append(HttpHeaders.Authorization, "Bearer ${client.tokens.oauthToken}")
        append("appkey", client.appKey)
        @Suppress("SpellCheckingInspection") append("appsecret", client.appSecret)
    }
}

internal fun HttpRequestBuilder.setStock(ticker: String) {
    url {
        parameters.run @Suppress("SpellCheckingInspection") {
            append("FID_COND_MRKT_DIV_CODE", "J")
            append("FID_INPUT_ISCD", ticker)
        }
    }
}

internal fun HttpRequestBuilder.setSector(ticker: String) {
    url {
        parameters.run @Suppress("SpellCheckingInspection") {
            append("FID_COND_MRKT_DIV_CODE", "U")
            append("FID_INPUT_ISCD", ticker)
        }
    }
}

internal fun HttpRequestBuilder.setTradeId(tradeId: String) {
    headers {
        append("tr_id", tradeId)
    }
}

internal fun HttpRequestBuilder.setCorporation(corp: CorporationRequest?) = corp?.let {
    headers @Suppress("SpellCheckingInspection") {
        it.globalUID?.let { append("gt_uid", it) }
        it.consumerType?.let { append("custtype", it.num) }
        it.phoneNumber?.let { append("phone_number", it.replace("-", "").trim()) }
        it.ipAddr?.let { append("ip_addr", it.replace(":", "").trim()) }
        it.personalSecKey?.let { append("personalseckey", it) }
        append("seq_no", if (it.consumerType == ConsumerTypeCode.Personal) "" else "01")
    }
}

internal fun <T : Response> T.processHeader(res: HttpResponse) {
    res.headers.forEach { s, strings ->
        when (s) {
            "tr_id" -> if (this is TradeIdMsg) this.tradeId = strings[0]
            "tr_cont" -> if (this is TradeContinuousResponse<*>) this.tradeContinuous = strings[0]
            "gt_uid" -> if (this is TradeIdMsg) this.globalTradeID = strings[0]
        }
    }
}

@Suppress("SpellCheckingInspection")
/**
 * 요청의 결과를 검증하고 [Result]에 담아 반환합니다.
 * 일반적인 경우 [Result]에 결과를 담아 반환합니다.
 * 에러가 발생했을 경우 [Result]에 에러를 담아 반환합니다.
 *
 * @return 요청의 결과
 */
internal fun <T : Response> T.validateAndGet(): Result<T> {
    if (this.errorDescription != null || this.errorCode != null) {
        return Result(
            this,
            RequestException(errorDescription ?: "Unknown error", errorCode?.let { RequestCode.fromCode(it) })
        )
    }
    if ((this as? Msg)?.isOk == false) {
        return Result(
            this,
            RequestException((this as Msg).msg ?: "Unknown error", code?.let { RequestCode.fromCode(it) })
        )
    }
    return Result(this)
}

internal inline fun <reified T : Response, reified U : Data> T.setupContinuous(
    processedData: U,
    noinline continuousModifier: U.(T) -> U,
    req: DataRequest<U, T>
) {
    if (this !is TradeContinuousResponse<*>) return
    if (this.tradeContinuous != "F" && this.tradeContinuous != "M") return

    val nextData = processedData.continuousModifier(this).apply {
        tradeContinuous = "N"
    }

    @Suppress("UNCHECKED_CAST")
    (this as TradeContinuousResponse<T>).next = {
        req.call(nextData)
    }
}

@Suppress("SpellCheckingInspection")
/**
 * 요청을 수행하고 [Result]에 담아 반환합니다.
 * @param data 요청에 필요한 데이터
 * @param bodyModifier 요청 결과를 수정하는 함수
 * @param continuousModifier 연속 조회를 위해 요청 데이터를 수정하는 함수
 * @param block 요청을 수행하는 함수
 */
internal suspend inline fun <reified T : Response, reified U : Data> DataRequest<U, T>.request(
    data: U,
    noinline bodyModifier: (T) -> T = { it },
    noinline continuousModifier: U.(T) -> U = { this },
    crossinline block: suspend (U) -> HttpResponse,
): Result<T> {
    return try {
        val req = this@request
        val processedData = data.apply {
            this.corp = this.corp ?: req.client.corpRequest
        }

        client.options.rateLimiter.rated {
            val resp = block(processedData)
            resp.body<T>()
                .apply {
                    processHeader(resp)
                    setupContinuous(processedData, continuousModifier, req)
                }
                .let { bodyModifier(it) }
                .validateAndGet()
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: RequestException) {
        Result(null, e)
    } catch (e: Exception) {
        val errorMsg = "${e::class.qualifiedName}: ${e.message ?: "Unknown error"}"
        Result(null, RequestException(errorMsg, RequestCode.Unknown, cause = e))
    }
}

@Suppress("SpellCheckingInspection")
/**
 * 요청을 수행하고 [Result]에 담아 반환합니다.
 * @param bodyModifier 요청 결과를 수정하는 함수
 * @param block 요청을 수행하는 함수
 */
internal suspend inline fun <reified T : Response> NoDataRequest<T>.request(
    noinline bodyModifier: ((T) -> T)? = null,
    crossinline block: suspend () -> HttpResponse
): Result<T> {
    return try {
        client.options.rateLimiter.rated {
            val resp = block()
            resp.body<T>()
                .apply { processHeader(resp) }
                .let { if (bodyModifier != null) bodyModifier(it) else it }
                .validateAndGet()
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: RequestException) {
        Result(null, e)
    } catch (e: Exception) {
        val errorMsg = "${e::class.qualifiedName}: ${e.message ?: "Unknown error"}"
        Result(null, RequestException(errorMsg, RequestCode.Unknown, cause = e))
    }
}