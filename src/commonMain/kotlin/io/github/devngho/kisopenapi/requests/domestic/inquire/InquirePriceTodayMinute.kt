package io.github.devngho.kisopenapi.requests.domestic.inquire

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
import io.github.devngho.kisopenapi.requests.response.stock.price.domestic.StockPriceBase
import io.github.devngho.kisopenapi.requests.response.stock.price.domestic.StockPriceChange
import io.github.devngho.kisopenapi.requests.response.stock.price.domestic.StockPriceForeigner
import io.github.devngho.kisopenapi.requests.response.stock.price.domestic.StockPriceHighMax
import io.github.devngho.kisopenapi.requests.response.stock.trade.StockTrade
import io.github.devngho.kisopenapi.requests.response.stock.trade.StockTradeAccumulate
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.HHMMSSSerializer.HHMMSS
import io.github.devngho.kisopenapi.requests.util.YNSerializer.YN
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("unused")
class InquirePriceTodayMinute(override val client: KISApiClient) :
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
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        var output1: InquirePriceTodayMinuteResponseOutput1?,
        var output2: List<InquirePriceTodayMinuteResponseOutput2>?,
        override var next: (suspend () -> Result<InquirePriceTodayMinuteResponse>)?
    ) : Response, TradeContinuousResponse<InquirePriceTodayMinuteResponse>, TradeIdMsg {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class InquirePriceTodayMinuteResponseOutput1(
        @SerialName("prdy_vrss") @Contextual override val changeFromYesterday: BigInteger?,
        @SerialName("prdy_vrss_sign") override val signFromYesterday: SignPrice?,
        @SerialName("prdy_ctrt") @Contextual override val rateFromYesterday: BigDecimal?,
        @SerialName("stck_prdy_clpr") @Contextual val priceFinalYesterday: BigInteger?,
        @SerialName("acml_prtt_rate") @Contextual val accumulateDivisionRate: BigDecimal?,
        @SerialName("hts_frgn_ehrt") @Contextual override val htsForeignerExhaustionRate: BigDecimal?,
        @SerialName("frgn_ntby_qty") @Contextual override val foreignerNetBuyCount: BigInteger?,
        @SerialName("acml_vol") @Contextual override val accumulateTradeVolume: BigInteger?,
        @SerialName("prdy_vrss_vol_rate") @Contextual override val rateTradeVolumeFromYesterday: BigDecimal?,
        @SerialName("stck_prpr") @Contextual override val price: BigInteger?
    ): StockPriceBase, StockTrade, StockPriceForeigner, StockPriceChange {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class InquirePriceTodayMinuteResponseOutput2(
        @SerialName("stck_bsop_date") val bizDate: String?,
        @SerialName("stck_cntg_hour") val stockConfirmTime: String?,
        @SerialName("prdy_vrss_vol_rate") @Contextual override val rateTradeVolumeFromYesterday: BigDecimal?,
        @SerialName("acml_vol") @Contextual override val accumulateTradeVolume: BigInteger?,
        @SerialName("acml_tr_pbmn") @Contextual override val accumulateTradePrice: BigInteger?,
        @SerialName("stck_prpr") @Contextual override val price: BigInteger?,
        @SerialName("stck_oprc") @Contextual override val openingPrice: BigInteger?,
        @SerialName("stck_hgpr") @Contextual override val highPrice: BigInteger?,
        @SerialName("stck_lwpr") @Contextual override val lowPrice: BigInteger?,
        @SerialName("cntg_vol") @Contextual val confirmVolume: BigInteger?
    ) : StockPriceHighMax, StockTradeAccumulate {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    data class InquirePriceTodayMinuteData(
        override val ticker: String,
        val startTime: Time,
        val usePreviousData: Boolean,
        override var corp: CorporationRequest? = null, override var tradeContinuous: String? = ""
    ) : Data, TradeContinuousData, Ticker

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: InquirePriceTodayMinuteData) = request(data) {
        client.httpClient.get(url) {
            setAuth(client)
            setTradeId("FHKST03010200")
            setStock(it.ticker)
            setCorporation(it.corp)

            url { _ ->
                parameters.run {
                    set("FID_ETC_CLS_CODE", "")
                    set("FID_INPUT_HOUR_1", it.startTime.HHMMSS)
                    set("FID_PW_DATA_INCU_YN", it.usePreviousData.YN)
                }
            }
        }
    }
}