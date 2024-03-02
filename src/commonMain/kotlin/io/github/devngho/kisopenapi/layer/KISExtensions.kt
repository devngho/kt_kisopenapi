package io.github.devngho.kisopenapi.layer

import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.util.Currency
import io.github.devngho.kisopenapi.requests.util.OverseasMarket

inline fun KISApiClient.stockDomestic(ticker: String): StockDomestic {
    return StockDomesticImpl(this, ticker)
}

inline fun KISApiClient.stockOverseas(ticker: String, market: OverseasMarket): StockOverseas {
    return StockOverseasImpl(this, ticker, market)
}

inline fun KISApiClient.accountDomestic(): AccountDomestic {
    return AccountDomesticImpl(this)
}

inline fun KISApiClient.accountOverseas(market: OverseasMarket, currency: Currency): AccountOverseas {
    return AccountOverseasImpl(this, market, currency)
}

inline fun KISApiClient.marketDomestic(): MarketDomestic {
    return MarketDomestic(this)
}

inline fun KISApiClient.krx() = marketDomestic()

inline fun KISApiClient.marketOverseas(market: OverseasMarket): MarketOverseas {
    return MarketOverseas(this, market)
}