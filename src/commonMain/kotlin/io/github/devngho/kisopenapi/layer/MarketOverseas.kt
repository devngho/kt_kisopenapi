package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.overseas.inquire.InquireOverseasCondition
import io.github.devngho.kisopenapi.requests.util.OverseasMarket
import io.github.devngho.kisopenapi.requests.util.Result

class MarketOverseas(val api: KISApiClient, val exchange: OverseasMarket) : Market {
    suspend fun search(query: Market.StockSearchQuery<BigDecimal>): Result<List<StockOverseas>> {
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

    suspend fun search(queryBuilder: (Market.StockSearchQuery<BigDecimal>.() -> Unit)): Result<List<StockOverseas>> =
        search(Market.StockSearchQuery.stockSearchQuery(queryBuilder))
}