package io.github.devngho.kisopenapi.requests

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.response.*
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class InquireOverseasPrice(override val client: KisOpenApi):
    DataRequest<InquireOverseasPrice.InquirePriceData, InquireOverseasPrice.InquirePriceResponse> {
    private val url = if (client.isDemo) "https://openapivts.koreainvestment.com:29443/uapi/overseas-price/v1/quotations/price"
                        else             "https://openapi.koreainvestment.com:9443/uapi/overseas-price/v1/quotations/price"

    @Serializable
    data class InquirePriceResponse(
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("tr_cont") var tradeCount: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        var output: InquirePriceResponseOutput?, override var next: (suspend () -> Response)?,
        override var tradeContinuous: String?
    ): Response, TradeContinuousResponse, TradeIdMsg {
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
    override suspend fun call(data: InquirePriceData): InquirePriceResponse = client.rateLimiter.rated {
        if (data.corp == null) data.corp = client.corp

        fun HttpRequestBuilder.inquirePrice() {
            auth(client)
            url {
                parameters.run {
                    append("AUTH", "")
                    append("EXCD", data.market.code)
                    append("SYMB", data.ticker)
                }
            }
            tradeId("HHDFS00000300")
            data.corp?.let { corporation(it) }
        }

        val res = client.httpClient.get(url) {
            inquirePrice()
        }

        res.body<InquirePriceResponse>().let {
            // 변화량이 음수인 경우에도 changeFromYesterday 값이 양수로 반환됨
            // 편의성 위해 변동률 음수인 경우 changeFromYesterday 값도 음수로 변환함
            if (it.output?.rateFromYesterday?.isNegative == true) it.copy(output = it.output!!.copy(changeFromYesterday = it.output!!.changeFromYesterday!! * -1))
            else it
        }.apply {
            if (this.errorCode != null) throw RequestError(this.errorDescription)

            processHeader(res)
            setNext(data, this)
        }
    }
}