package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.BigNumber
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.overseas.inquire.InquireOverseasCondition
import io.github.devngho.kisopenapi.requests.util.OverseasMarket
import io.github.devngho.kisopenapi.requests.util.Result
import kotlinx.serialization.ExperimentalSerializationApi

class MarketOverseas(val api: KISApiClient, val exchange: OverseasMarket) : Market {
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

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun search(query: StockSearchQuery<BigDecimal>): Result<List<StockOverseas>> {
        val res = InquireOverseasCondition(api).call(
            InquireOverseasCondition.ConditionData(
                exchange = exchange,
                priceRange = query.priceRange,
                rateFromYesterdayRange = query.rateFromYesterdayRange,
                tradeVolumeRange = query.tradeVolumeRange,
                perRange = query.perRange,
                epsRange = query.epsRange,
                tradePriceVolumeRange = query.tradePriceVolumeRange,
                shareRange = query.shareRange,
                marketCapRange = query.marketCapRange
            )
        )

        res
            .also { if (!it.isOk) return Result(null, it.error) }
            .getOrThrow().output!!
            .mapNotNull { api.stockOverseas(it.ticker ?: return@mapNotNull null, exchange) }
            .let { return Result(it) }
    }

    suspend fun search(queryBuilder: (StockSearchQuery<BigDecimal>.() -> Unit)): Result<List<StockOverseas>> =
        search(StockSearchQuery.stockSearchQuery(queryBuilder))
}