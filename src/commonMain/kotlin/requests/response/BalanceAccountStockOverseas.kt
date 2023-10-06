package io.github.devngho.kisopenapi.requests.response

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.util.LoanType
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface BalanceAccountStockOverseas: Response, StockOverseasPriceBase {
    @SerialName("ovrs_pdno")
    val ticker: String?
    @SerialName("ovrs_item_name") val productName: String?
    @SerialName("ovrs_cblc_qty") @Contextual val count: BigInteger?
    @SerialName("ord_psbl_qty") @Contextual val countCanOrder: BigInteger?
    @SerialName("pchs_avg_pric") @Contextual val buyAveragePrice: BigDecimal?
    @SerialName("frcr_pchs_amt1") @Contextual val buyAmountByForeignCurrency: BigDecimal?
    @SerialName("now_pric2") @Contextual override val price: BigDecimal?
    @SerialName("ovrs_stck_evlu_amt") @Contextual val evalAmount: BigDecimal?
    @SerialName("frcr_evlu_pfls_amt") @Contextual val evalProfitLossAmount: BigDecimal?
    @SerialName("evlu_pfls_rt") @Contextual val evalProfitLossRate: BigDecimal?
    @SerialName("loan_dt") val loanDate: String?
    @OptIn(ExperimentalSerializationApi::class)
    @SerialName("loan_type_cd") @Serializable(with = LoanType.LoanTypeSerializer::class) val loanType: LoanType?
    @SerialName("expd_dt") val expireDate: String?
}