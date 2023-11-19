package io.github.devngho.kisopenapi.requests.response

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import io.github.devngho.kisopenapi.requests.Response
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

/**
 * [KIS Developers 문서](https://apiportal.koreainvestment.com/apiservice/apiservice-overseas-stock)를 참조하세요.
 * @see io.github.devngho.kisopenapi.requests.InquireOverseasBalance
 */
@Suppress("unused")
interface BalanceAccountOverseas: Response {
    /** 외화 매입 금액1 */
    @SerialName("frcr_pchs_amt1") @Contextual val buyAmountByForeignCurrency: BigDecimal?

    /** 해외 실현 손익 금액 */
    @SerialName("ovrs_rlzt_pfls_amt") @Contextual val realizedProfitLossAmount: BigDecimal?

    /** 해외 총 손익 금액 */
    @SerialName("ovrs_tot_pfls") @Contextual val totalProfitLoss: BigDecimal?

    /** 실현 수익률 */
    @SerialName("rlzt_erng_rt") @Contextual val realizedProfitRate: BigDecimal?

    /** 총 평가 손익 금액 */
    @SerialName("tot_evlu_pfls_amt") @Contextual val totalProfitLossAmount: BigDecimal?

    /** 총 평가 손익률 */
    @SerialName("tot_pftrt") @Contextual val totalProfitLossRate: BigDecimal?

    /** 외화 매수 금액 합계1 */
    @SerialName("frcr_buy_amt_smtl1") @Contextual val buyAmountTotalByForeignCurrency: BigDecimal?

    /** 해외 실현 손익 금액2 */
    @SerialName("ovrs_rlzt_pfls_amt2") @Contextual val realizedProfitLossAmount2: BigDecimal?

    /** 외화 매수 금액 합계2 */
    @SerialName("frcr_buy_amt_smtl2") @Contextual val buyAmountTotalByForeignCurrency2: BigDecimal?
}