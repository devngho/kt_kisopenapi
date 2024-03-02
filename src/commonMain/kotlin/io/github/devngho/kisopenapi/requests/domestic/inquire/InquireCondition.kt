package io.github.devngho.kisopenapi.requests.domestic.inquire

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Data
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.data.TradeIdMsg
import io.github.devngho.kisopenapi.requests.response.stock.Ticker
import io.github.devngho.kisopenapi.requests.response.stock.price.domestic.StockPrice
import io.github.devngho.kisopenapi.requests.response.stock.price.domestic.StockPriceChange
import io.github.devngho.kisopenapi.requests.response.stock.trade.StockTradeAccumulate
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 국내 주식 종목 검색 조건을 사용해 검색하고 반환합니다.
 */
@DemoNotSupported
class InquireCondition(override val client: KISApiClient) :
    DataRequest<InquireCondition.ConditionData, InquireCondition.ConditionResponse> {
    private val url = "${client.options.baseUrl}/uapi/domestic-stock/v1/quotations/psearch-result"

    @Serializable
    data class ConditionResponse(
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        @SerialName("output2") var output: List<ConditionResponseOutput>?,
    ) : Response, TradeIdMsg {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class ConditionResponseOutput(
        @SerialName("code") override val ticker: String?,
        @SerialName("name") val name: String?,
        @SerialName("daebi") override val sign: SignPrice?,
        @SerialName("price") @Serializable(with = BigIntegerFromDecimalSerializer::class) override val price: BigInteger?,
        @SerialName("chgrate") @Contextual override val rate: BigDecimal?,
        @SerialName("acml_vol") @Serializable(with = BigIntegerFromDecimalSerializer::class) override val accumulateTradeVolume: BigInteger?,
        @SerialName("change") @Serializable(with = BigIntegerFromDecimalSerializer::class) override val change: BigInteger?,
        @SerialName("trade_amt") @Serializable(with = BigIntegerFromDecimalSerializer::class) override val accumulateTradePrice: BigInteger?,
        @SerialName("cttr") @Serializable(with = BigIntegerFromDecimalSerializer::class) val confirmPower: BigInteger?,
        @SerialName("open") @Serializable(with = BigIntegerFromDecimalSerializer::class) override val openingPrice: BigInteger?,
        @SerialName("high") @Serializable(with = BigIntegerFromDecimalSerializer::class) override val highestPrice: BigInteger?,
        @SerialName("low") @Serializable(with = BigIntegerFromDecimalSerializer::class) override val lowestPrice: BigInteger?,
        @SerialName("high52") @Serializable(with = BigIntegerFromDecimalSerializer::class) val high52WeekPrice: BigInteger?,
        @SerialName("low52") @Serializable(with = BigIntegerFromDecimalSerializer::class) val low52WeekPrice: BigInteger?,
        @SerialName("expprice") @Serializable(with = BigIntegerFromDecimalSerializer::class) val expectedPrice: BigInteger?,
        @SerialName("expchange") @Serializable(with = BigIntegerFromDecimalSerializer::class) val expectedChange: BigInteger?,
        @SerialName("expchggrate") @Contextual val expectedChangeRate: BigDecimal?,
        @SerialName("expcvol") @Serializable(with = BigIntegerFromDecimalSerializer::class) val expectedConfirmVolume: BigInteger?,
        @SerialName("chgrate2") @Contextual override val rateTradeVolumeFromYesterday: BigDecimal?,
        @SerialName("expdaebi") val expectedSignPrice: SignPrice?,
        @SerialName("recprice") @Serializable(with = BigIntegerFromDecimalSerializer::class) val criteriaPrice: BigInteger?,
        @SerialName("uplmtprice") @Serializable(with = BigIntegerFromDecimalSerializer::class) override val maxPrice: BigInteger?,
        @SerialName("dnlmtprice") @Serializable(with = BigIntegerFromDecimalSerializer::class) override val minPrice: BigInteger?,
        @SerialName("stotprice") @Serializable(with = BigIntegerFromDecimalSerializer::class) val marketCap: BigInteger?,
    ) : Ticker, StockPrice, StockPriceChange, StockTradeAccumulate {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    data class ConditionData(
        val conditionKey: String,
        override var corp: CorporationRequest? = null,
    ) : Data

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: ConditionData) = request(data) {
        if (client.isDemo) throw RequestException(
            "모의투자에서는 사용할 수 없는 API InquireCondition을 호출했습니다.",
            RequestCode.DemoUnavailable
        )

        client.httpClient.get(url) {
            setAuth(client)
            setTradeId("HHKST03900400")
            setCorporation(it.corp)

            url { _ ->
                parameters.run {
                    set("user_id", client.htsId ?: return@run)
                    set("seq", data.conditionKey)
                }
            }
        }
    }
}