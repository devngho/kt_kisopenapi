package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.InquireLivePrice
import io.github.devngho.kisopenapi.requests.OrderBuy
import io.github.devngho.kisopenapi.requests.response.StockPriceBase
import io.github.devngho.kisopenapi.requests.response.StockTrade
import io.github.devngho.kisopenapi.requests.util.Closeable
import io.github.devngho.kisopenapi.requests.util.OrderTypeCode


interface IStockDomestic : IStockBase{
    override val client: KisOpenApi
    override val code: String

    var price: StockPriceBase
    override var name: IStockBase.Name
    var tradeVolume: StockTrade

    suspend fun buy(count: BigInteger, type: OrderTypeCode, price: BigInteger = BigInteger(0)): OrderBuy.OrderResponse
    suspend fun sell(count: BigInteger, type: OrderTypeCode, price: BigInteger = BigInteger(0)): OrderBuy.OrderResponse
    suspend fun useLiveConfirmPrice(block: Closeable.(InquireLivePrice.InquireLivePriceResponse) -> Unit)
}