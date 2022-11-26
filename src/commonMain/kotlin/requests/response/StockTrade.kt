package io.github.devngho.kisopenapi.requests.response

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.requests.Response
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

interface StockTrade: Response {
    @SerialName("prdy_vrss_vol_rate") @Contextual val rateTradeVolumeFromYesterday: BigDecimal?
    @SerialName("acml_vol") @Contextual val accumulateTradeVolume: BigInteger?
}