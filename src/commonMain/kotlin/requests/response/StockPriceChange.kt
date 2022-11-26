package io.github.devngho.kisopenapi.requests.response

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.requests.util.SignYesterday
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

interface StockPriceChange {
    @SerialName("prdy_vrss") @Contextual val changeFromYesterday: BigInteger?
    @SerialName("prdy_vrss_sign") val signFromYesterday: SignYesterday?
    @SerialName("prdy_ctrt") @Contextual val rateFromYesterday: BigDecimal?
}