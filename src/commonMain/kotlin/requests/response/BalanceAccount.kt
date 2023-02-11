package io.github.devngho.kisopenapi.requests.response

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.util.YNSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface BalanceAccount: Response {
    @SerialName("dnca_tot_amt") @Contextual val depositReceivedTotalAmount: BigInteger?
    @SerialName("nxdy_excc_amt") @Contextual val execAmountNextDay: BigInteger?
    @SerialName("prvs_rcdl_excc_amt") @Contextual val domesticExecAmount: BigInteger?
    @SerialName("cma_evlu_amt") @Contextual val cmaEvalAmount: BigInteger?
    @SerialName("bfdy_buy_amt") @Contextual val buyAmountFromYesterday: BigInteger?
    @SerialName("thdt_buy_amt") @Contextual val buyAmountToday: BigInteger?
    @SerialName("nxdy_auto_rdpt_amt") @Contextual val autoRepayNextDayAmount: BigInteger?
    @SerialName("bfdy_sll_amt") @Contextual val sellAmountFromYesterday: BigInteger?
    @SerialName("thdt_sll_amt") @Contextual val sellAmountToday: BigInteger?
    @SerialName("d2_auto_rdpt_amt") @Contextual val autoRepayD2Amount: BigInteger?
    @SerialName("bfdy_tlex_amt") @Contextual val tlexAmountFromYesterday: BigInteger?
    @SerialName("thdt_tlex_amt") @Contextual val tlexAmountToday: BigInteger?
    @SerialName("tot_loan_amt") @Contextual val totalLoanAmount: BigInteger?
    @SerialName("scts_evlu_amt") @Contextual val sctsEvalAmount: BigInteger?
    @SerialName("tot_evlu_amt") @Contextual val totalEvalAmount: BigInteger?
    @SerialName("nass_amt") @Contextual val netWorthAmount: BigInteger?
    @SerialName("fncg_gld_auto_rdpt_yn") @Serializable(with = YNSerializer::class) val loanAutoRepay: Boolean?
    @SerialName("pchs_amt_smtl_amt") @Contextual val buyTotalAmount: BigInteger?
    @SerialName("evlu_amt_smtl_amt") @Contextual val evalTotalAmount: BigInteger?
    @SerialName("evlu_pfls_smtl_amt") @Contextual val evalProfitLossTotalAmount: BigInteger?
    @SerialName("tot_sltn_slng_chgs") @Contextual val totalShortSellingAmount: BigInteger?
    @SerialName("bfdy_tot_asst_evlu_amt") @Contextual val totalEvalAssetAmountFromYesterday: BigInteger?
    @SerialName("asst_icdc_amt") @Contextual val assetChangeAmount: BigInteger?
    @SerialName("asst_icdc_erng_rt") @Contextual val assetChangeRate: BigDecimal?
    @SerialName("rlzt_pfls") @Contextual val realizedProfitLoss: BigInteger?
    @SerialName("rlzt_erng_rt") @Contextual val realizedProfitLossRate: BigDecimal?
    @SerialName("rlzt_evlu_pfls") @Contextual val realEvalProfitLoss: BigInteger?
    @SerialName("rlzt_evlu_pfls_erng_rt") @Contextual val realEvalProfitLossRate: BigDecimal?
}