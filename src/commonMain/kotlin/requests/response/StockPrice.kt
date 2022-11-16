package io.github.devngho.kisopenapi.requests.response

import io.github.devngho.kisopenapi.requests.Response
import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

interface StockPrice: StockPriceHighMax, Response {
    @SerialName("stck_mxpr") @Contextual val maxPrice: BigInteger?
    @SerialName("stck_llam") @Contextual val minPrice: BigInteger?
}