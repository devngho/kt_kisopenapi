package io.github.devngho.kisopenapi.requests.response

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import io.github.devngho.kisopenapi.requests.Response
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

@Suppress("unused")
interface BalanceAccountOverseas: Response {
    @SerialName("frcr_pchs_amt1") @Contextual val buyAmountByForeignCurrency: BigDecimal?
    @SerialName("ovrs_rlzt_pfls_amt") @Contextual val realizedProfitLossAmount: BigDecimal?
    @SerialName("ovrs_tot_pfls") @Contextual val totalProfitLoss: BigDecimal?
    @SerialName("rlzt_erng_rt") @Contextual val realizedProfitRate: BigDecimal?
    @SerialName("tot_evlu_pfls_amt") @Contextual val totalProfitLossAmount: BigDecimal?
    @SerialName("tot_pftrt") @Contextual val totalProfitLossRate: BigDecimal?
    @SerialName("frcr_buy_amt_smtl1") @Contextual val buyAmountTotalByForeignCurrency: BigDecimal?
    @SerialName("ovrs_rlzt_pfls_amt2") @Contextual val realizedProfitLossAmount2: BigDecimal?
    @SerialName("frcr_buy_amt_smtl2") @Contextual val buyAmountTotalByForeignCurrency2: BigDecimal?
}