package io.github.devngho.kisopenapi.layer

import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.response.stock.StockProductInfo


sealed interface StockBase : Updatable {
    val client: KISApiClient
    val ticker: String
    val info: StockProductInfo
}