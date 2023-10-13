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

class InquireConfirm(override val client: KisOpenApi):
    DataRequest<InquireConfirm.InquireConfirmData, InquireConfirm.InquireConfirmResponse> {
    private val url = if (client.isDemo) "https://openapivts.koreainvestment.com:29443/uapi/domestic-stock/v1/quotations/inquire-ccnl"
                      else               "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/inquire-ccnl"

    @Serializable
    data class InquireConfirmResponse(
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("tr_cont") override var tradeContinuous: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        var output: List<InquireConfirmResponseOutput>?, override var next: (suspend () -> Response)?
    ): Response, TradeContinuousResponse, TradeIdMsg {
        override val errorDescription: String? = null
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
        val stockCode: String,
        /** 기본적으로 KisOpenApi corp 값을 불러옵니다. */
        override var corp: CorporationRequest? = null,
        override var tradeContinuous: String? = ""
    ) : Data, TradeContinuousData

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: InquireConfirmData): InquireConfirmResponse = client.rateLimiter.rated {
        if (data.corp == null) data.corp = client.corp

        val res = client.httpClient.get(url) {
            auth(client)
            tradeId("FHKST01010300")
            stock(data.stockCode)
            data.corp?.let { corporation(it) }
        }
        res.body<InquireConfirmResponse>().apply {
            if (this.errorCode != null) throw RequestError(this.errorDescription)

            processHeader(res)
            setNext(data, this)
        }
    }
}