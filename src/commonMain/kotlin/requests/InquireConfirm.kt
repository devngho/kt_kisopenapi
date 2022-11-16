package io.github.devngho.kisopenapi.requests

import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.response.StockPriceBase
import io.github.devngho.kisopenapi.requests.response.TradeContinuousData
import io.github.devngho.kisopenapi.requests.response.TradeContinuousResponse
import io.github.devngho.kisopenapi.requests.util.*
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class InquireConfirm(override val client: KisOpenApi):
    DataRequest<InquireConfirm.InquireConfirmData, InquireConfirm.InquireConfirmResponse> {
    private val url = if (client.isDemo) "https://openapivts.koreainvestment.com:29443/uapi/domestic-stock/v1/quotations/inquire-ccnl"
                      else               "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/inquire-ccnl"

    @Serializable
    data class InquireConfirmResponse(
        @SerialName("tr_id") var tradeId: String?,
        @SerialName("tr_cont") override var tradeContinuous: String?,
        @SerialName("gt_uid") var globalTradeID: String?,
        @SerialName("msg_cd") val code: String?,
        @SerialName("msg1") val msg: String?,

        var output: List<InquireConfirmResponseOutput>?, override var next: (suspend () -> Response)?
    ): Response, TradeContinuousResponse {
        override val error_description: String? = null
        override val error_code: String? = null
    }

    @Serializable
    data class InquireConfirmResponseOutput(
        @SerialName("stck_cntg_hour") val stockConfirmHour: Int,
        @SerialName("stck_prpr") @Contextual override val price: BigInteger,
        @SerialName("prdy_vrss") @Contextual override val changeFromYesterday: BigInteger,
        @SerialName("prdy_vrss_sign") override val signFromYesterday: SignYesterday,
        @SerialName("cntg_vol") @Contextual val confirmVolume: BigInteger,
        @SerialName("tday_rltv") @Contextual val todayConfirmPowerVolume: BigDecimal,
        @SerialName("prdy_ctrt") @Contextual override val rateFromYesterday: BigDecimal,
    ): StockPriceBase {
        override val error_description: String? = null
        override val error_code: String? = null
    }

    data class InquireConfirmData(val stockCode: String, override val tradeContinuous: String? = ""): Data, TradeContinuousData

    override suspend fun call(data: InquireConfirmData): InquireConfirmResponse {
        val res = client.httpClient.get(url) {
            auth(client)
            tradeId("FHKST01010300")
            stock(data.stockCode)
        }
        return res.body<InquireConfirmResponse>().apply {
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