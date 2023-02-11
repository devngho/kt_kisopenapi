package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.InquireLivePrice
import io.github.devngho.kisopenapi.requests.OrderBuy
import io.github.devngho.kisopenapi.requests.response.StockPriceBase
import io.github.devngho.kisopenapi.requests.response.StockTrade
import io.github.devngho.kisopenapi.requests.util.Closeable
import io.github.devngho.kisopenapi.requests.util.OrderTypeCode


interface IStock : Updatable{
    val client: KisOpenApi
    val code: String

    data class Name(
        var name: String? = null,
        var name120: String? = null,
        var nameEng: String? = null,
        var nameEng120: String? = null,
        var nameShort: String? = null,
        var nameEngShort: String? = null
    )

    var price: StockPriceBase
    var name: Name
    var tradeVolume: StockTrade

    suspend fun buy(count: BigInteger, type: OrderTypeCode, price: BigInteger = BigInteger(0)): OrderBuy.OrderResponse
    suspend fun sell(count: BigInteger, type: OrderTypeCode, price: BigInteger = BigInteger(0)): OrderBuy.OrderResponse
    suspend fun useLiveConfirmPrice(block: Closeable.(InquireLivePrice.InquireLivePriceResponse) -> Unit)
}