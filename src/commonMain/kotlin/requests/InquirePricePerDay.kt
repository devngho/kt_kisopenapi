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

class InquirePricePerDay(override val client: KisOpenApi):
    DataRequest<InquirePricePerDay.InquirePricePerDayData, InquirePricePerDay.InquirePricePerDayResponse> {
    private val url = if (client.isDemo) "https://openapivts.koreainvestment.com:29443/uapi/domestic-stock/v1/quotations/inquire-daily-price"
                      else               "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/inquire-daily-price"

    @Serializable
    data class InquirePricePerDayResponse(
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("tr_cont") override var tradeContinuous: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        var output: List<InquirePricePerDayResponseOutput>?, override var next: (suspend () -> Response)?
    ): Response, TradeContinuousResponse, TradeIdMsg {
        override val errorDescription: String? = null
        override val errorCode: String? = null
    }

    @Serializable
    data class InquirePricePerDayResponseOutput(
        @SerialName("stck_bsop_date") val bizDate: String?,
        @SerialName("stck_oprc") @Contextual override val openingPrice: BigInteger?,
        @SerialName("stck_hgpr") @Contextual override val highPrice: BigInteger?,
        @SerialName("stck_lwpr") @Contextual override val lowPrice: BigInteger?,
        /**
         * Close Price
         */
        @SerialName("stck_clpr") @Contextual override val price: BigInteger?,
        @SerialName("prdy_vrss") @Contextual override val changeFromYesterday: BigInteger?,
        @SerialName("prdy_vrss_sign") override val signFromYesterday: SignPrice?,
        @SerialName("prdy_ctrt") @Contextual override val rateFromYesterday: BigDecimal?,
        @SerialName("flng_cls_code") val lockCode: LockCode?,
        @SerialName("acml_prtt_rate") @Contextual val accumulateDivisionRate: BigDecimal?,
        @SerialName("acml_vol") @Contextual override val accumulateTradeVolume: BigInteger?,
        @SerialName("hts_frgn_ehrt") @Contextual override val htsForeignerExhaustionRate: BigDecimal?,
        @SerialName("frgn_ntby_qty") @Contextual override val foreignerNetBuyCount: BigInteger?,
        @SerialName("prdy_vrss_vol_rate") @Contextual override val rateTradeVolumeFromYesterday: BigDecimal?
    ): StockPriceHighMax, StockTrade, StockPriceChange, StockPriceForeigner {
        override val errorDescription: String? = null
        override val errorCode: String? = null
    }

    data class InquirePricePerDayData(val stockCode: String, val period: PeriodDivisionCode = PeriodDivisionCode.Days30, val useOriginalPrice: Boolean = false,
                                      override var corp: CorporationRequest? = null, override var tradeContinuous: String? = ""): Data, TradeContinuousData

    override suspend fun call(data: InquirePricePerDayData): InquirePricePerDayResponse {
        if (data.corp == null) data.corp = client.corp

        val res = client.httpClient.get(url) {
            auth(client)
            tradeId("FHKST01010400")
            stock(data.stockCode)
            data.corp?.let { corporation(it) }

            url {
                parameters.run {
                    append("FID_PERIOD_DIV_CODE", data.period.num)
                    append("FID_ORG_ADJ_PRC", if (data.useOriginalPrice) "1" else "0")
                }
            }
        }
        return res.body<InquirePricePerDayResponse>().apply {
            if (this.errorCode != null) throw RequestError(this.errorDescription)

            processHeader(res)
            setNext(data, this)
        }
    }
}