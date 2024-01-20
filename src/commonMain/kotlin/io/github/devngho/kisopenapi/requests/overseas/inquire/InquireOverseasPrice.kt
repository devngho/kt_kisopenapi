package io.github.devngho.kisopenapi.requests.overseas.inquire

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Data
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.data.TradeContinuousData
import io.github.devngho.kisopenapi.requests.data.TradeContinuousResponse
import io.github.devngho.kisopenapi.requests.data.TradeIdMsg
import io.github.devngho.kisopenapi.requests.response.stock.Ticker
import io.github.devngho.kisopenapi.requests.response.stock.price.overseas.StockOverseasPrice
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 해외 주식 종목의 가격을 조회하고 반환합니다.
 */
class InquireOverseasPrice(override val client: KISApiClient) :
    DataRequest<InquireOverseasPrice.InquirePriceData, InquireOverseasPrice.InquirePriceResponse> {
    private val url = "${client.options.baseUrl}/uapi/overseas-price/v1/quotations/price"

    @Serializable
    data class InquirePriceResponse(
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("tr_cont") var tradeCount: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        var output: InquirePriceResponseOutput?, override var next: (suspend () -> Result<InquirePriceResponse>)?,
        override var tradeContinuous: String?
    ) : Response, TradeContinuousResponse<InquirePriceResponse>, TradeIdMsg {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class InquirePriceResponseOutput(
        @SerialName("rsym") val liveLoadCode: String?,
        @SerialName("zdiv") override val decimalPoint: Int?,
        @SerialName("base") @Contextual val priceYesterday: BigDecimal?,
        @SerialName("pvol") @Contextual val tradeVolumeYesterday: BigInteger?,
        @SerialName("last") @Contextual override val price: BigDecimal?,
        @SerialName("sign") override val sign: SignPrice?,
        @SerialName("diff") @Contextual override val changeFromYesterday: BigDecimal?,
        @SerialName("rate") @Contextual override val rateFromYesterday: BigDecimal?,
        @SerialName("tvol") @Contextual override val tradeVolume: BigInteger?,
        @SerialName("tamt") @Contextual override val tradePriceVolume: BigDecimal?,
        /**
         * 거래 가능 여부입니다. True/False나 Y/N이 아니므로 주의하시기 바랍니다.
         */
        @SerialName("ordy") @Contextual val canBuy: String?,
    ): StockOverseasPrice {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    data class InquirePriceData(
        override val ticker: String,
        val market: OverseasMarket,
        override var corp: CorporationRequest? = null,
        override var tradeContinuous: String? = ""
    ) : Data, TradeContinuousData, Ticker

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: InquirePriceData) = request(data, block = {
        client.httpClient.get(url) {
            setAuth(client)
            url { _ ->
                parameters.run {
                    append("AUTH", "")
                    append("EXCD", it.market.code)
                    append("SYMB", it.ticker)
                }
            }
            setTradeId("HHDFS00000300")
            setCorporation(it.corp)
        }
    }, bodyModifier = {
        // 실제 변화량이 음수인 경우에도 changeFromYesterday 값이 양수로 반환됨
        // 편의성 위해 변동률이 음수이고 변화량은 양수인 경우 경우 changeFromYesterday 값을 음수로 변환함
        if (it.output?.rateFromYesterday?.isNegative == true && it.output?.changeFromYesterday?.isPositive == true) it.copy(
            output = it.output!!.copy(changeFromYesterday = it.output!!.changeFromYesterday!! * -1)
        )
        else it
    })
}