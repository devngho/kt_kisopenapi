package io.github.devngho.kisopenapi.requests

import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.util.*
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.requests.response.*
import io.github.devngho.kisopenapi.requests.util.YNSerializer.YN
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("unused")
class InquirePriceTodayMinute(override val client: KisOpenApi):
    DataRequest<InquirePriceTodayMinute.InquirePriceTodayMinuteData, InquirePriceTodayMinute.InquirePriceTodayMinuteResponse> {
    private val url = if (client.isDemo) "https://openapivts.koreainvestment.com:29443/uapi/domestic-stock/v1/quotations/inquire-daily-price"
                      else               "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/inquire-daily-price"

    @Serializable
    data class InquirePriceTodayMinuteResponse(
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("tr_cont") override var tradeContinuous: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,

        var output1: InquirePriceTodayMinuteResponseOutput1?,
        var output2: List<InquirePriceTodayMinuteResponseOutput2>?, override var next: (suspend () -> Response)?
    ): Response, TradeContinuousResponse, Msg {
        override val error_description: String? = null
        override val error_code: String? = null
    }

    @Serializable
    data class InquirePriceTodayMinuteResponseOutput1(
        @SerialName("prdy_vrss") @Contextual override val changeFromYesterday: BigInteger?,
        @SerialName("prdy_vrss_sign") override val signFromYesterday: SignYesterday?,
        @SerialName("prdy_ctrt") @Contextual override val rateFromYesterday: BigDecimal?,
        @SerialName("stck_prdy_clpr") @Contextual val priceFinalYesterday: BigInteger?,
        @SerialName("acml_prtt_rate") @Contextual val accumulateDivisionRate: BigDecimal?,
        @SerialName("hts_frgn_ehrt") @Contextual override val htsForeignerExhaustionRate: BigDecimal?,
        @SerialName("frgn_ntby_qty") @Contextual override val foreignerNetBuyCount: BigInteger?,
        @SerialName("acml_vol") @Contextual override val accumulateTradeVolume: BigInteger?,
        @SerialName("prdy_vrss_vol_rate") @Contextual override val rateTradeVolumeFromYesterday: BigDecimal?,
        @SerialName("stck_prpr") @Contextual override val price: BigInteger?
    ): StockPriceBase, StockTrade, StockPriceForeigner, StockPriceChange {
        override val error_description: String? = null
        override val error_code: String? = null
    }

    @Serializable
    data class InquirePriceTodayMinuteResponseOutput2(
        @SerialName("stck_bsop_date") val bizDate: String?,
        @SerialName("stck_cntg_hour") val stockConfirmTime: String?,
        @SerialName("prdy_vrss_vol_rate") @Contextual override val rateTradeVolumeFromYesterday: BigDecimal?,
        @SerialName("acml_vol") @Contextual override val accumulateTradeVolume: BigInteger?,
        @SerialName("acml_tr_pbmn") @Contextual override val accumulateTradePrice: BigInteger?,
        @SerialName("stck_prpr") @Contextual override val price: BigInteger?,
        @SerialName("stck_oprc") @Contextual override val marketPrice: BigInteger?,
        @SerialName("stck_hgpr") @Contextual override val highPrice: BigInteger?,
        @SerialName("stck_lwpr") @Contextual override val lowPrice: BigInteger?,
        @SerialName("cntg_vol") @Contextual val confirmVolume: BigInteger?
        ): StockPriceHighMax, StockTradeAccumulate {
        override val error_description: String? = null
        override val error_code: String? = null
    }

    data class InquirePriceTodayMinuteData(val stockCode: String,/** Time style : HHMMSS */val startDate: String, val usePreviousData: Boolean,
                                           override var corp: CorporationRequest? = null, override val tradeContinuous: String? = ""): Data, TradeContinuousData

    override suspend fun call(data: InquirePriceTodayMinuteData): InquirePriceTodayMinuteResponse {
        if (data.corp == null) data.corp = client.corp

        val res = client.httpClient.get(url) {
            auth(client)
            tradeId("FHKST03010200")
            stock(data.stockCode)
            data.corp?.let { corporation(it) }

            url {
                parameters.run {
                    set("FID_ETC_CLS_CODE", "")
                    set("FID_INPUT_HOUR_1", data.startDate)
                    set("FID_PW_DATA_INCU_YN", data.usePreviousData.YN)
                }
            }
        }
        return res.body<InquirePriceTodayMinuteResponse>().apply {
            if (this.error_code != null) throw RequestError(this.error_description)

            res.headers.forEach { s, strings ->
                when(s) {
                    "tr_id" -> this.tradeId = strings[0]
                    "tr_cont" -> this.tradeContinuous = strings[0]
                    "gt_uid" -> this.globalTradeID = strings[0]
                }
            }

            if (this.tradeContinuous == "F" || this.tradeContinuous == "M") {
                this.next = {
                    call(data.copy(tradeContinuous = "N"))
                }
            }
        }
    }
}