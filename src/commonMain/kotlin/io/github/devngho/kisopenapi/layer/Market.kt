package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.BigNumber
import com.ionspin.kotlin.bignum.integer.BigInteger

sealed interface Market {
    class StockSearchQuery<T> where T : Comparable<Any>, T : BigNumber<T> {
        var priceRange: ClosedRange<T>? = null
        var rateFromYesterdayRange: ClosedRange<T>? = null
        var tradeVolumeRange: BigInteger.BigIntegerRange? = null
        var perRange: ClosedRange<T>? = null
        var epsRange: ClosedRange<T>? = null
        var tradePriceVolumeRange: ClosedRange<T>? = null
        var shareRange: BigInteger.BigIntegerRange? = null
        var marketCapRange: ClosedRange<T>? = null

        companion object {
            fun <T> stockSearchQuery(block: StockSearchQuery<T>.() -> Unit): StockSearchQuery<T> where T : Comparable<Any>, T : BigNumber<T> {
                return StockSearchQuery<T>().apply(block)
            }
        }
    }
}