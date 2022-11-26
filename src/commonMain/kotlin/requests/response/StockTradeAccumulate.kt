package io.github.devngho.kisopenapi.requests.response

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

interface StockTradeAccumulate: StockTrade {
    @SerialName("acml_tr_pbmn") @Contextual val accumulateTradePrice: BigInteger?
}