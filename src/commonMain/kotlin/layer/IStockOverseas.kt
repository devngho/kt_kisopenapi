package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.InquireOverseasLivePrice
import io.github.devngho.kisopenapi.requests.OrderOverseasBuy
import io.github.devngho.kisopenapi.requests.response.StockOverseasPrice
import io.github.devngho.kisopenapi.requests.util.Closeable
import io.github.devngho.kisopenapi.requests.util.OrderTypeCode
import io.github.devngho.kisopenapi.requests.util.OverseasMarket


interface IStockOverseas : IStockBase{
    override val client: KisOpenApi
    override val code: String
    val market: OverseasMarket

    var price: StockOverseasPrice
    override var name: IStockBase.Name

    suspend fun buy(count: BigInteger, type: OrderTypeCode, price: BigDecimal = BigDecimal.fromInt(0)): OrderOverseasBuy.OrderResponse
    suspend fun sell(count: BigInteger, type: OrderTypeCode, price: BigDecimal = BigDecimal.fromInt(0)): OrderOverseasBuy.OrderResponse
    suspend fun useLiveConfirmPrice(block: Closeable.(InquireOverseasLivePrice.InquireLivePriceResponse) -> Unit)
}