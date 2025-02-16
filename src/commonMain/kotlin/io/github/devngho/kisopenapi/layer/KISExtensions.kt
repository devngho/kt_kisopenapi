package io.github.devngho.kisopenapi.layer

import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.util.Currency
import io.github.devngho.kisopenapi.requests.util.InternalApi
import io.github.devngho.kisopenapi.requests.util.OverseasMarket

fun KISApiClient.stockDomestic(ticker: String): StockDomestic {
    return StockDomesticImpl(this, ticker)
}

fun KISApiClient.stockOverseas(ticker: String, market: OverseasMarket): StockOverseas {
    return StockOverseasImpl(this, ticker, market)
}

@OptIn(InternalApi::class)
fun KISApiClient.accountDomestic(): AccountDomestic {
    return AccountDomesticImpl(this)
}

@OptIn(InternalApi::class)
fun KISApiClient.accountOverseas(market: OverseasMarket, currency: Currency): AccountOverseas {
    return AccountOverseasImpl(this, market, currency)
}

fun KISApiClient.marketDomestic(): MarketDomestic {
    return MarketDomestic(this)
}

fun KISApiClient.krx() = marketDomestic()

fun KISApiClient.marketOverseas(market: OverseasMarket): MarketOverseas {
    return MarketOverseas(this, market)
}