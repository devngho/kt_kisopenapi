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
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 국내 주식 종목의 최근 체결 정보를 조회하고 반환합니다.
 */
class InquireConfirm(override val client: KISApiClient) :
    DataRequest<InquireConfirm.InquireConfirmData, InquireConfirm.InquireConfirmResponse> {
    private val url = "${client.options.baseUrl}/uapi/domestic-stock/v1/quotations/inquire-ccnl"

    @Serializable
    data class InquireConfirmResponse(
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("tr_cont") override var tradeContinuous: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        var output: List<InquireConfirmResponseOutput>?,
        override var next: (suspend () -> Result<InquireConfirmResponse>)?
    ) : Response, TradeContinuousResponse<InquireConfirmResponse>, TradeIdMsg {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class InquireConfirmResponseOutput(
        @SerialName("stck_cntg_hour") val stockConfirmHour: Int?,
        @SerialName("stck_prpr") @Contextual override val price: BigInteger?,
        @SerialName("prdy_vrss") @Contextual override val changeFromYesterday: BigInteger?,
        @SerialName("prdy_vrss_sign") override val signFromYesterday: SignPrice?,
        @SerialName("cntg_vol") @Contextual val confirmVolume: BigInteger?,
        @SerialName("tday_rltv") @Contextual val todayConfirmPowerVolume: BigDecimal?,
        @SerialName("prdy_ctrt") @Contextual override val rateFromYesterday: BigDecimal?,
    ): StockPriceBase, StockPriceChange

    data class InquireConfirmData(
        override val ticker: String,
        /** 기본적으로 KisOpenApi corp 값을 불러옵니다. */
        override var corp: CorporationRequest? = null,
        override var tradeContinuous: String? = ""
    ) : Data, TradeContinuousData, Ticker

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: InquireConfirmData) = request(data) {
        client.httpClient.get(url) {
            setAuth(client)
            setTradeId("FHKST01010300")
            setStock(it.ticker)
            setCorporation(it.corp)
        }
    }
}