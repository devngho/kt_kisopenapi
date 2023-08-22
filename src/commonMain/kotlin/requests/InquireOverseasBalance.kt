package io.github.devngho.kisopenapi.requests

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.response.*
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.OverseasMarket.Companion.fourChar
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class InquireOverseasBalance(override val client: KisOpenApi):
    DataRequest<InquireOverseasBalance.InquireBalanceData, InquireOverseasBalance.InquireBalanceResponse> {
    private val url = if (client.isDemo) "https://openapivts.koreainvestment.com:29443/uapi/overseas-stock/v1/trading/inquire-balance"
                        else             "https://openapi.koreainvestment.com:9443/uapi/overseas-stock/v1/trading/inquire-balance"

    @Serializable
    data class InquireBalanceResponse(
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,
        @SerialName("ctx_area_fk200") val continuousAreaFK: String?,
        @SerialName("ctx_area_nk200") val continuousAreaNK: String?,

        var output1: List<InquireBalanceResponseOutput1>?,
        var output2: InquireBalanceResponseOutput2?,
        override var next: (suspend () -> Response)?,
        @SerialName("tr_cont") override var tradeContinuous: String?
    ): Response, TradeContinuousResponse, TradeIdMsg {
        override val errorDescription: String? = null
        override val errorCode: String? = null
    }

    @Serializable
    data class InquireBalanceResponseOutput1 @OptIn(ExperimentalSerializationApi::class) constructor(
        @SerialName("ovrs_pdno") override val productCode: String?,
        @SerialName("ovrs_item_name") override val productName: String?,
        @SerialName("ovrs_cblc_qty") @Contextual override val count: BigInteger?,
        @SerialName("ord_psbl_qty") @Contextual override val countCanOrder: BigInteger?,
        @SerialName("pchs_avg_pric") @Contextual override val buyAveragePrice: BigDecimal?,
        @SerialName("frcr_pchs_amt1") @Contextual override val buyAmountByForeignCurrency: BigDecimal?,
        @SerialName("now_pric2") @Contextual override val price: BigDecimal?,
        @SerialName("ovrs_stck_evlu_amt") @Contextual override val evalAmount: BigDecimal?,
        @SerialName("frcr_evlu_pfls_amt") @Contextual override val evalProfitLossAmount: BigDecimal?,
        @SerialName("evlu_pfls_rt") @Contextual override val evalProfitLossRate: BigDecimal?,
        @SerialName("loan_dt") override val loanDate: String?,
        @SerialName("loan_type_cd") @Serializable(with = LoanType.LoanTypeSerializer::class) override val loanType: LoanType?,
        @SerialName("expd_dt") override val expireDate: String?,
    ): BalanceAccountStockOverseas {
        override val errorDescription: String? = null
        override val errorCode: String? = null
    }

    @Serializable
    data class InquireBalanceResponseOutput2(
        @SerialName("frcr_pchs_amt1") @Contextual val buyAmountByForeignCurrency: BigDecimal?,
        @SerialName("ovrs_rlzt_pfls_amt") @Contextual val realizedProfitLossAmount: BigDecimal?,
        @SerialName("ovrs_tot_pfls") @Contextual val totalProfitLoss: BigDecimal?,
        @SerialName("rlzt_erng_rt") @Contextual val realizedProfitRate: BigDecimal?,
        @SerialName("tot_evlu_pfls_amt") @Contextual val totalProfitLossAmount: BigDecimal?,
        @SerialName("tot_pftrt") @Contextual val totalProfitLossRate: BigDecimal?,
        @SerialName("frcr_buy_amt_smtl1") @Contextual val buyAmountTotalByForeignCurrency: BigDecimal?,
        @SerialName("ovrs_rlzt_pfls_amt2") @Contextual val realizedProfitLossAmount2: BigDecimal?,
        @SerialName("frcr_buy_amt_smtl2") @Contextual val buyAmountTotalByForeignCurrency2: BigDecimal?,
    ): Response {
        override val errorDescription: String? = null
        override val errorCode: String? = null
    }

    data class InquireBalanceData(
        val marketCode: OverseasMarket,
        val currencyCode: Currency,
        override var corp: CorporationRequest? = null,
        override val tradeContinuous: String? = "",
        val continuousAreaFK: String = "",
        val continuousAreaNK: String = ""
    ) : Data, TradeContinuousData

    override suspend fun call(data: InquireBalanceData): InquireBalanceResponse {
        if (data.corp == null) data.corp = client.corp

        fun HttpRequestBuilder.inquireOverseasBalance() {
            auth(client)
            tradeId(if(client.isDemo) "VTTS3012R" else "TTTS3012R")
            data.corp?.let { corporation(it) }

            url {
                parameters.run {
                    set("CANO", client.account!![0])
                    set("ACNT_PRDT_CD", client.account!![1])
                    set("OVRS_EXCG_CD", data.marketCode.fourChar)
                    set("TR_CRCY_CD", data.currencyCode.code)
                    set("CTX_AREA_FK200", data.continuousAreaFK)
                    set("CTX_AREA_NK200", data.continuousAreaNK)
                }
            }
        }

        val res = client.httpClient.get(url) {
            inquireOverseasBalance()
        }
        return res.body<InquireBalanceResponse>().apply {
            if (this.errorCode != null) throw RequestError(this.errorDescription)

            res.headers.forEach { s, strings ->
                when(s) {
                    "tr_id" -> this.tradeId = strings[0]
                    "tr_cont" -> this.tradeContinuous = strings[0]
                    "gt_uid" -> this.globalTradeID = strings[0]
                }
            }

            if (this.tradeContinuous == "F" || this.tradeContinuous == "M") {
                this.next = {
                    call(data.copy(tradeContinuous = "N", continuousAreaFK = this.continuousAreaFK!!, continuousAreaNK = this.continuousAreaNK!!))
                }
            }
        }
    }
}