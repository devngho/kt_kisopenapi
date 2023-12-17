package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.overseas.inquire.live.InquireOverseasLivePrice
import io.github.devngho.kisopenapi.requests.overseas.order.OrderOverseasAmend
import io.github.devngho.kisopenapi.requests.overseas.order.OrderOverseasBuy
import io.github.devngho.kisopenapi.requests.overseas.order.OrderOverseasCancel
import io.github.devngho.kisopenapi.requests.response.stock.price.overseas.StockOverseasPriceBase
import io.github.devngho.kisopenapi.requests.util.Closeable
import io.github.devngho.kisopenapi.requests.util.OrderTypeCode
import io.github.devngho.kisopenapi.requests.util.OverseasMarket
import io.github.devngho.kisopenapi.requests.util.Result
import kotlin.jvm.JvmStatic


interface StockOverseas : StockBase {
    override val client: KISApiClient
    override val ticker: String
    val market: OverseasMarket

    var price: StockOverseasPriceBase
    override var name: StockBase.Name

    suspend fun buy(
        count: BigInteger,
        type: OrderTypeCode,
        price: BigDecimal = BigDecimal.fromInt(0)
    ): Result<OrderOverseasBuy.OrderResponse>

    suspend fun sell(
        count: BigInteger,
        type: OrderTypeCode,
        price: BigDecimal = BigDecimal.fromInt(0)
    ): Result<OrderOverseasBuy.OrderResponse>

    suspend fun amend(
        order: OrderOverseasBuy.OrderResponse,
        count: BigInteger,
        type: OrderTypeCode,
        price: BigDecimal = BigDecimal.fromInt(0)
    ): Result<OrderOverseasAmend.OrderResponse>

    suspend fun cancel(
        order: OrderOverseasBuy.OrderResponse,
        count: BigInteger,
        type: OrderTypeCode
    ): Result<OrderOverseasCancel.OrderResponse>

    suspend fun useLiveConfirmPrice(block: Closeable.(InquireOverseasLivePrice.InquireOverseasLivePriceResponse) -> Unit)

    companion object {
        @JvmStatic
        fun create(client: KISApiClient, market: OverseasMarket, ticker: String): StockOverseas =
            StockOverseasImpl(client, ticker, market)
    }
}