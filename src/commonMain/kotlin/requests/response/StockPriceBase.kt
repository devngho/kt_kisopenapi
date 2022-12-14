package io.github.devngho.kisopenapi.requests.response

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

interface StockPriceBase {
    @SerialName("stck_prpr") @Contextual val price: BigInteger?
}