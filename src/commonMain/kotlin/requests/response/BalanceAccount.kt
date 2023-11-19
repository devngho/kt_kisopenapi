package io.github.devngho.kisopenapi.requests.response

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.util.YNSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [KIS Developers 문서](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock)를 참조하세요.
 * @see io.github.devngho.kisopenapi.requests.InquireBalance
 */
interface BalanceAccount: Response {
    /** 예수금 */
    @SerialName("dnca_tot_amt") @Contextual val depositReceivedTotalAmount: BigInteger?

    /** 익일 정산 금액 (D+1 예수금) */
    @SerialName("nxdy_excc_amt") @Contextual val execAmountNextDay: BigInteger?

    /** 가정산 금액 (D+2 예수금) */
    @SerialName("prvs_rcdl_excc_amt") @Contextual val domesticExecAmount: BigInteger?

    /** CMA 평가 금액 */
    @SerialName("cma_evlu_amt") @Contextual val cmaEvalAmount: BigInteger?

    /** 전일 매수 금액 */
    @SerialName("bfdy_buy_amt") @Contextual val buyAmountFromYesterday: BigInteger?

    /** 당일 매수 금액 */
    @SerialName("thdt_buy_amt") @Contextual val buyAmountToday: BigInteger?

    /** 익일 자동 상환 금액 */
    @SerialName("nxdy_auto_rdpt_amt") @Contextual val autoRepayNextDayAmount: BigInteger?

    /** 전일 매도 금액 */
    @SerialName("bfdy_sll_amt") @Contextual val sellAmountFromYesterday: BigInteger?

    /** 당일 매도 금액 */
    @SerialName("thdt_sll_amt") @Contextual val sellAmountToday: BigInteger?

    /** D+2 자동 상환 금액 */
    @SerialName("d2_auto_rdpt_amt") @Contextual val autoRepayD2Amount: BigInteger?

    /** 전일제비용금액 */
    @SerialName("bfdy_tlex_amt") @Contextual val tlexAmountFromYesterday: BigInteger?

    /** 당일제비용금액 */
    @SerialName("thdt_tlex_amt") @Contextual val tlexAmountToday: BigInteger?

    /** 총 대출 금액 */
    @SerialName("tot_loan_amt") @Contextual val totalLoanAmount: BigInteger?

    /** 유가증권 평가 금액 */
    @SerialName("scts_evlu_amt") @Contextual val sctsEvalAmount: BigInteger?

    /** 총 평가 금액
     * (유가증권 평가 금액) + (D+2 예수금) */
    @SerialName("tot_evlu_amt") @Contextual val totalEvalAmount: BigInteger?

    /** 순자산 금액 */
    @SerialName("nass_amt") @Contextual val netWorthAmount: BigInteger?

    /** 융자금 자동 상환 여부 */
    @SerialName("fncg_gld_auto_rdpt_yn") @Serializable(with = YNSerializer::class) val loanAutoRepay: Boolean?

    /** 매입 금액 합계 */
    @SerialName("pchs_amt_smtl_amt") @Contextual val buyTotalAmount: BigInteger?

    /** 평가 금액 합계(유가증권 평가 금액 합계) */
    @SerialName("evlu_amt_smtl_amt") @Contextual val evalTotalAmount: BigInteger?

    /** 평가 손익 금액 합계 */
    @SerialName("evlu_pfls_smtl_amt") @Contextual val evalProfitLossTotalAmount: BigInteger?

    /** 총 대주 매각 금액 */
    @SerialName("tot_sltn_slng_chgs") @Contextual val totalShortSellingAmount: BigInteger?

    /** 전일 총 자산 평가 금액 */
    @SerialName("bfdy_tot_asst_evlu_amt") @Contextual val totalEvalAssetAmountFromYesterday: BigInteger?

    /** 자산 변동 금액 */
    @SerialName("asst_icdc_amt") @Contextual val assetChangeAmount: BigInteger?

    /** 자산 변동률(데이터 미제공) */
    @SerialName("asst_icdc_erng_rt") @Contextual val assetChangeRate: BigDecimal?

    /** 실현 수익 금액 */
    @SerialName("rlzt_pfls") @Contextual val realizedProfitLoss: BigInteger?

    /** 실현 수익률 */
    @SerialName("rlzt_erng_rt") @Contextual val realizedProfitLossRate: BigDecimal?

    /** 실평가손익 */
    @SerialName("real_evlu_pfls_erng_rt")
    @Contextual
    val realEvalProfitLoss: BigInteger?

    /** 실평가손익률 */
    @SerialName("real_evlu_pfls_erng_rt")
    @Contextual
    val realEvalProfitLossRate: BigDecimal?
}