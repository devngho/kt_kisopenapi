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
import io.github.devngho.kisopenapi.requests.response.balance.overseas.BalanceAccountStockOverseas
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.OverseasMarket.Companion.fourChar
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 해외 주식의 잔고를 조회하고 반환합니다.
 */
class InquireOverseasBalance(override val client: KISApiClient) :
    DataRequest<InquireOverseasBalance.InquireBalanceData, InquireOverseasBalance.InquireBalanceResponse> {
    private val url = "${client.options.baseUrl}/uapi/overseas-stock/v1/trading/inquire-balance"

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
        override var next: (suspend () -> Result<InquireBalanceResponse>)?,
        @SerialName("tr_cont") override var tradeContinuous: String?
    ) : Response, TradeContinuousResponse<InquireBalanceResponse>, TradeIdMsg {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class InquireBalanceResponseOutput1 @OptIn(ExperimentalSerializationApi::class) constructor(
        @SerialName("ovrs_pdno") override val ticker: String?,
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
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
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
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    data class InquireBalanceData(
        val marketCode: OverseasMarket,
        val currencyCode: Currency,
        override var corp: CorporationRequest? = null,
        override var tradeContinuous: String? = "",
        val continuousAreaFK: String = "",
        val continuousAreaNK: String = ""
    ) : Data, TradeContinuousData

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: InquireBalanceData) = request(data, block = {
        client.httpClient.get(url) {
            setAuth(client)
            setTradeId(if (client.isDemo) "VTTS3012R" else "TTTS3012R")
            setCorporation(it.corp)

            url { _ ->
                parameters.run {
                    set("CANO", client.account!!.first)
                    set("ACNT_PRDT_CD", client.account!!.second)
                    set("OVRS_EXCG_CD", it.marketCode.fourChar)
                    set("TR_CRCY_CD", it.currencyCode.code)
                    set("CTX_AREA_FK200", it.continuousAreaFK)
                    set("CTX_AREA_NK200", it.continuousAreaNK)
                }
            }
        }
    }, continuousModifier = {
        data.copy(continuousAreaFK = it.continuousAreaFK!!, continuousAreaNK = it.continuousAreaNK!!)
    })
}