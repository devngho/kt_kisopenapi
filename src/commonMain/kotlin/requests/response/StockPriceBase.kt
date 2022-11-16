package io.github.devngho.kisopenapi.requests.response

import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.util.SignYesterday
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

interface StockPriceBase: Response {
    @SerialName("stck_prpr") @Contextual val price: BigInteger?
    @SerialName("prdy_vrss") @Contextual val changeFromYesterday: BigInteger?
    @SerialName("prdy_vrss_sign") val signFromYesterday: SignYesterday?
    @SerialName("prdy_ctrt") @Contextual val rateFromYesterday: BigDecimal?
}