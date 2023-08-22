package io.github.devngho.kisopenapi.requests

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.response.*
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.YNSerializer.YN
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class InquireBalance(override val client: KisOpenApi):
    DataRequest<InquireBalance.InquireBalanceData, InquireBalance.InquireBalanceResponse> {
    private val url = if (client.isDemo) "https://openapivts.koreainvestment.com:29443/uapi/domestic-stock/v1/trading/inquire-balance"
                        else             "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/trading/inquire-balance"

    @Serializable
    data class InquireBalanceResponse(
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,
        @SerialName("ctx_area_fk100") val continuousAreaFK: String?,
        @SerialName("ctx_area_nk100") val continuousAreaNK: String?,

        var output1: List<InquireBalanceResponseOutput1>?,
        var output2: List<InquireBalanceResponseOutput2>?,
        override var next: (suspend () -> Response)?,
        @SerialName("tr_cont") override var tradeContinuous: String?
    ): Response, TradeContinuousResponse, TradeIdMsg {
        override val errorDescription: String? = null
        override val errorCode: String? = null
    }

    @Serializable
    data class InquireBalanceResponseOutput1(
        @SerialName("pdno") override val productCode: String?,
        @SerialName("prdt_name") override val productName: String?,
        @SerialName("trad_dvsn_name") override val buySellDivision: String?,
        @SerialName("bfdy_buy_qty") @Contextual override val buyCountYesterday: BigInteger?,
        @SerialName("bfdy_sll_qty") @Contextual override val sellCountYesterday: BigInteger?,
        @SerialName("thdt_buy_qty") @Contextual override val buyCountToday: BigInteger?,
        @SerialName("thdt_sll_qty") @Contextual override val sellCountToday: BigInteger?,
        @SerialName("hldg_qty") @Contextual override val count: BigInteger?,
        @SerialName("ord_psbl_qty") @Contextual override val countCanOrder: BigInteger?,
        @SerialName("pchs_avg_pric") @Contextual override val buyAveragePrice: BigDecimal?,
        @SerialName("pchs_amt") @Contextual override val buyAmount: BigInteger?,
        @SerialName("prpr") @Contextual override val price: BigInteger?,
        @SerialName("evlu_amt") @Contextual override val evalAmount: BigInteger?,
        @SerialName("evlu_pfls_amt") @Contextual override val evalProfitLossAmount: BigInteger?,
        @SerialName("evlu_pfls_rt") @Contextual override val evalProfitLossRate: BigDecimal?,
        @SerialName("evlu_erng_rt") @Contextual override val evalProfitRate: BigDecimal?,
        @SerialName("loan_dt") override val loanDate: String?,
        @SerialName("loan_amt") @Contextual override val loanAmount: BigInteger?,
        @SerialName("stln_slng_chgs") @Contextual override val amountShortSelling: BigInteger?,
        @SerialName("expd_dt") override val expireDate: String?,
        @SerialName("fltt_rt") @Contextual override val changeRate: BigDecimal?,
        @SerialName("bfdy_cprs_icdc") @Contextual override val changeFromYesterday: BigInteger?,
        @SerialName("item_mgna_rt_name") override val stockMarginRateName: String?,
        @SerialName("grta_rt_name") override val depositRateName: String?,
        @SerialName("sbst_pric") @Contextual override val substitutePrice: BigInteger?,
        @SerialName("stck_loan_unpr") @Contextual override val stockLoanPrice: BigInteger?,
    ): BalanceAccountStock {
        override val errorDescription: String? = null
        override val errorCode: String? = null
    }

    @Serializable
    data class InquireBalanceResponseOutput2(
        @SerialName("dnca_tot_amt") @Contextual override val depositReceivedTotalAmount: BigInteger?,
        @SerialName("nxdy_excc_amt") @Contextual override val execAmountNextDay: BigInteger?,
        @SerialName("prvs_rcdl_excc_amt") @Contextual override val domesticExecAmount: BigInteger?,
        @SerialName("cma_evlu_amt") @Contextual override val cmaEvalAmount: BigInteger?,
        @SerialName("bfdy_buy_amt") @Contextual override val buyAmountFromYesterday: BigInteger?,
        @SerialName("thdt_buy_amt") @Contextual override val buyAmountToday: BigInteger?,
        @SerialName("nxdy_auto_rdpt_amt") @Contextual override val autoRepayNextDayAmount: BigInteger?,
        @SerialName("bfdy_sll_amt") @Contextual override val sellAmountFromYesterday: BigInteger?,
        @SerialName("thdt_sll_amt") @Contextual override val sellAmountToday: BigInteger?,
        @SerialName("d2_auto_rdpt_amt") @Contextual override val autoRepayD2Amount: BigInteger?,
        @SerialName("bfdy_tlex_amt") @Contextual override val tlexAmountFromYesterday: BigInteger?,
        @SerialName("thdt_tlex_amt") @Contextual override val tlexAmountToday: BigInteger?,
        @SerialName("tot_loan_amt") @Contextual override val totalLoanAmount: BigInteger?,
        @SerialName("scts_evlu_amt") @Contextual override val sctsEvalAmount: BigInteger?,
        @SerialName("tot_evlu_amt") @Contextual override val totalEvalAmount: BigInteger?,
        @SerialName("nass_amt") @Contextual override val netWorthAmount: BigInteger?,
        @SerialName("fncg_gld_auto_rdpt_yn") @Serializable(with = YNSerializer::class) override val loanAutoRepay: Boolean?,
        @SerialName("pchs_amt_smtl_amt") @Contextual override val buyTotalAmount: BigInteger?,
        @SerialName("evlu_amt_smtl_amt") @Contextual override val evalTotalAmount: BigInteger?,
        @SerialName("evlu_pfls_smtl_amt") @Contextual override val evalProfitLossTotalAmount: BigInteger?,
        @SerialName("tot_sltn_slng_chgs") @Contextual override val totalShortSellingAmount: BigInteger?,
        @SerialName("bfdy_tot_asst_evlu_amt") @Contextual override val totalEvalAssetAmountFromYesterday: BigInteger?,
        @SerialName("asst_icdc_amt") @Contextual override val assetChangeAmount: BigInteger?,
        @SerialName("asst_icdc_erng_rt") @Contextual override val assetChangeRate: BigDecimal?,
        @SerialName("rlzt_pfls") @Contextual override val realizedProfitLoss: BigInteger?,
        @SerialName("rlzt_erng_rt") @Contextual override val realizedProfitLossRate: BigDecimal?,
        @SerialName("rlzt_evlu_pfls") @Contextual override val realEvalProfitLoss: BigInteger?,
        @SerialName("rlzt_evlu_pfls_erng_rt") @Contextual override val realEvalProfitLossRate: BigDecimal?,
    ): BalanceAccount {
        override val errorDescription: String? = null
        override val errorCode: String? = null
    }

    data class InquireBalanceData(
        val afterHourFinalPrice: Boolean = true,
        val inquireDivision: InquireDivisionCode = InquireDivisionCode.ByStock,
        val includeFund: Boolean = false,
        val includeYesterdaySell: Boolean = false,
        val includeCost: Boolean = false,
        override var corp: CorporationRequest? = null,
        override val tradeContinuous: String? = "",
        val continuousAreaFK: String = "",
        val continuousAreaNK: String = ""
    ) : Data, TradeContinuousData

    override suspend fun call(data: InquireBalanceData): InquireBalanceResponse {
        if (data.corp == null) data.corp = client.corp

        fun HttpRequestBuilder.InquireBalance() {
            auth(client)
            tradeId(if(client.isDemo) "VTTC8434R" else "TTTC8494R")
            data.corp?.let { corporation(it) }

            url {
                parameters.run {
                    set("CANO", client.account!![0])
                    set("ACNT_PRDT_CD", client.account!![1])
                    set("AFHR_FLPR_YN", data.afterHourFinalPrice.YN)
                    set("OFL_YN", "")
                    set("INQR_DVSN", data.inquireDivision.num)
                    set("UNPR_DVSN", "01")
                    set("FUND_STTL_ICLD_YN", data.includeFund.YN)
                    set("FNCG_AMT_AUTO_RDPT_YN", "N")
                    set("PRCS_DVSN", if (data.includeYesterdaySell) "00" else "01")
                    if (!client.isDemo) set("COST_ICLD_YN", data.includeCost.YN)
                    set("CTX_AREA_FK100", data.continuousAreaFK)
                    set("CTX_AREA_NK100", data.continuousAreaNK)
                }
            }
        }

        val res = client.httpClient.get(url) {
            InquireBalance()
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