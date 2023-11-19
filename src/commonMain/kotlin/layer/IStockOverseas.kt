package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.InquireOverseasLivePrice
import io.github.devngho.kisopenapi.requests.OrderOverseasAmend
import io.github.devngho.kisopenapi.requests.OrderOverseasBuy
import io.github.devngho.kisopenapi.requests.OrderOverseasCancel
import io.github.devngho.kisopenapi.requests.response.StockOverseasPriceBase
import io.github.devngho.kisopenapi.requests.util.Closeable
import io.github.devngho.kisopenapi.requests.util.OrderTypeCode
import io.github.devngho.kisopenapi.requests.util.OverseasMarket


interface IStockOverseas : IStockBase{
    override val client: KisOpenApi
    override val ticker: String
    val market: OverseasMarket

    var price: StockOverseasPriceBase
    override var name: IStockBase.Name

    suspend fun buy(count: BigInteger, type: OrderTypeCode, price: BigDecimal = BigDecimal.fromInt(0)): OrderOverseasBuy.OrderResponse
    suspend fun sell(count: BigInteger, type: OrderTypeCode, price: BigDecimal = BigDecimal.fromInt(0)): OrderOverseasBuy.OrderResponse
    suspend fun amend(
        order: OrderOverseasBuy.OrderResponse,
        count: BigInteger,
        type: OrderTypeCode,
        price: BigDecimal = BigDecimal.fromInt(0)
    ): OrderOverseasAmend.OrderResponse

    suspend fun cancel(
        order: OrderOverseasBuy.OrderResponse,
        count: BigInteger,
        type: OrderTypeCode
    ): OrderOverseasCancel.OrderResponse
    suspend fun useLiveConfirmPrice(block: Closeable.(InquireOverseasLivePrice.InquireLivePriceResponse) -> Unit)
}