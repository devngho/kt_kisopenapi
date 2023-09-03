package io.github.devngho.kisopenapi.requests.response

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.requests.Response
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

interface StockPriceHighMax : StockPriceBase, Response {
    @SerialName("stck_oprc") @Contextual val openingPrice: BigInteger?
    @SerialName("stck_hgpr") @Contextual val highPrice: BigInteger?
    @SerialName("stck_lwpr") @Contextual val lowPrice: BigInteger?
}