package io.github.devngho.kisopenapi.requests.response

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.requests.Response
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

interface BalanceAccountStock: Response, StockPriceBase {
    @SerialName("pdno")
    val ticker: String?
    @SerialName("prdt_name") val productName: String?
    @SerialName("trad_dvsn_name") val buySellDivision: String?
    @SerialName("bfdy_buy_qty") @Contextual val buyCountYesterday: BigInteger?
    @SerialName("bfdy_sll_qty") @Contextual val sellCountYesterday: BigInteger?
    @SerialName("thdt_buy_qty") @Contextual val buyCountToday: BigInteger?
    @SerialName("thdt_sll_qty") @Contextual val sellCountToday: BigInteger?
    @SerialName("hldg_qty") @Contextual val count: BigInteger?
    @SerialName("ord_psbl_qty") @Contextual val countCanOrder: BigInteger?
    @SerialName("pchs_avg_pric") @Contextual val buyAveragePrice: BigDecimal?
    @SerialName("pchs_amt") @Contextual val buyAmount: BigInteger?
    @SerialName("prpr") @Contextual override val price: BigInteger?
    @SerialName("evlu_amt") @Contextual val evalAmount: BigInteger?
    @SerialName("evlu_pfls_amt") @Contextual val evalProfitLossAmount: BigInteger?
    @SerialName("evlu_pfls_rt") @Contextual val evalProfitLossRate: BigDecimal?
    @SerialName("evlu_erng_rt") @Contextual val evalProfitRate: BigDecimal?
    @SerialName("loan_dt") val loanDate: String?
    @SerialName("loan_amt") @Contextual val loanAmount: BigInteger?
    @SerialName("stln_slng_chgs") @Contextual val amountShortSelling: BigInteger?
    @SerialName("expd_dt") val expireDate: String?
    @SerialName("fltt_rt") @Contextual val changeRate: BigDecimal?
    @SerialName("bfdy_cprs_icdc") @Contextual val changeFromYesterday: BigInteger?
    @SerialName("item_mgna_rt_name") val stockMarginRateName: String?
    @SerialName("grta_rt_name") val depositRateName: String?
    @SerialName("sbst_pric") @Contextual val substitutePrice: BigInteger?
    @SerialName("stck_loan_unpr") @Contextual val stockLoanPrice: BigInteger?
}