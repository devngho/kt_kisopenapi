package io.github.devngho.kisopenapi.requests.response

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

interface StockTradeRate: StockTrade {
    @SerialName("vol_tnrt") @Contextual val tradeVolumeTurningRate: BigDecimal?
}