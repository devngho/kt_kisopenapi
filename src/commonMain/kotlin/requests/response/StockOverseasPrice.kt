package io.github.devngho.kisopenapi.requests.response

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.requests.util.SignPrice
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

interface StockOverseasPrice: StockOverseasPriceBase {
    @SerialName("zdiv") val decimalPoint: Int?

    @SerialName("last")
    @Contextual
    override val price: BigDecimal?
    @SerialName("sign") val sign: SignPrice?
    @SerialName("diff") @Contextual val changeFromYesterday: BigDecimal?
    @SerialName("rate") @Contextual val rateFromYesterday: BigDecimal?
    @SerialName("tvol") @Contextual val tradeVolume: BigInteger?
    @SerialName("tamt") @Contextual val tradePriceVolume: BigDecimal?
}