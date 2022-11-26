package io.github.devngho.kisopenapi.requests.response

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

interface StockPriceForeigner: StockPriceBase {
    @SerialName("hts_frgn_ehrt") @Contextual val htsForeignerExhaustionRate: BigDecimal?
    @SerialName("frgn_ntby_qty") @Contextual val foreignerNetBuyCount: BigInteger?
}