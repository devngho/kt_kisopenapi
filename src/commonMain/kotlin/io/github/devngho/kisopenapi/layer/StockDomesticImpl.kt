package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.common.InquireProductBaseInfo
import io.github.devngho.kisopenapi.requests.domestic.inquire.InquirePrice
import io.github.devngho.kisopenapi.requests.domestic.inquire.InquireStockBaseInfo
import io.github.devngho.kisopenapi.requests.domestic.inquire.live.InquireLivePrice
import io.github.devngho.kisopenapi.requests.domestic.order.OrderAmend
import io.github.devngho.kisopenapi.requests.domestic.order.OrderBuy
import io.github.devngho.kisopenapi.requests.domestic.order.OrderCancel
import io.github.devngho.kisopenapi.requests.domestic.order.OrderSell
import io.github.devngho.kisopenapi.requests.response.stock.*
import io.github.devngho.kisopenapi.requests.response.stock.price.domestic.*
import io.github.devngho.kisopenapi.requests.response.stock.trade.*
import io.github.devngho.kisopenapi.requests.util.*
import kotlinx.coroutines.*
import kotlin.reflect.KClass


/**
 * 국내 주식 정보를 불러오고 관리합니다.
 * @param client KisOpenApi
 * @param ticker 종목 코드
 */
@OptIn(InternalApi::class)
class StockDomesticImpl(override val client: KISApiClient, override val ticker: String) : StockDomestic {
    override val price = UpdatableStockPriceFull()
    override val info = UpdatableStockProductInfo()
    override val tradeInfo = UpdatableStockTradeFull()


    override suspend fun update(vararg type: KClass<out Response>): Unit = coroutineScope {
        type.map { async { updateSingle(it) } }.awaitAll()
    }

    @OptIn(DemoNotSupported::class)
    private suspend fun updateSingle(type: KClass<out Response>) = coroutineScope {
        val jobs = mutableListOf<Deferred<Response?>>()

        when (type) {
            StockPriceFull::class,
            StockPrice::class,
            StockPriceBase::class,
            StockPriceChange::class,
            StockPriceForeigner::class,
            StockPriceLowHigh::class,
            StockTrade::class,
            StockTradeFull::class,
            StockTrade::class,
            StockTradeFull::class,
            StockTradeRate::class,
            StockTradeAccumulate::class -> {
                jobs += async { InquirePrice(client).call(InquirePrice.InquirePriceData(ticker)).getOrNull()?.output }
            }

            BaseProductInfo::class,
            ProductInfo::class -> {
                jobs += async {
                    InquireProductBaseInfo(client).call(
                        InquireProductBaseInfo.InquireProductBaseInfoData(
                            ticker,
                            ProductTypeCode.Stock
                        )
                    ).getOrNull()?.output
                }
            }

            StockInfo::class -> {
                jobs += async {
                    InquireStockBaseInfo(client).call(
                        InquireStockBaseInfo.InquireStockBaseInfoData(
                            ticker,
                            ProductTypeCode.Stock
                        )
                    ).getOrNull()?.output
                }
            }

            StockProductInfo::class -> {
                jobs += async {
                    InquireProductBaseInfo(client).call(
                    InquireProductBaseInfo.InquireProductBaseInfoData(
                        ticker,
                        ProductTypeCode.Stock
                    )
                    ).getOrNull()?.output
                }

                jobs += async {
                    InquireStockBaseInfo(client).call(
                        InquireStockBaseInfo.InquireStockBaseInfoData(
                            ticker,
                            ProductTypeCode.Stock
                        )
                    ).getOrNull()?.output
                }
            }

            else -> throw IllegalArgumentException("Unsupported type: $type. It might be a reason of unexpected behavior.")
        }

        jobs.awaitAll().forEach { it?.let { updateBy(it) } }
    }

    override fun updateBy(res: Response) {
        price.broadcast(res)
        info.broadcast(res)
        tradeInfo.broadcast(res)
    }

    override suspend fun buy(
        count: BigInteger,
        type: OrderTypeCode,
        market: MarketForOrder,
        price: BigInteger
    ): Result<OrderBuy.OrderResponse> =
        OrderBuy(client).call(OrderBuy.OrderData(ticker, type, count, price, market = market))

    override suspend fun sell(
        count: BigInteger,
        type: OrderTypeCode,
        market: MarketForOrder,
        price: BigInteger
    ): Result<OrderBuy.OrderResponse> =
        OrderSell(client).call(OrderBuy.OrderData(ticker, type, count, price, market = market))

    override suspend fun amend(
        order: OrderBuy.OrderResponse,
        count: BigInteger,
        type: OrderTypeCode,
        market: MarketForOrder,
        price: BigInteger,
        orderAll: Boolean
    ): Result<OrderAmend.OrderResponse> =
        OrderAmend(client).call(
            OrderAmend.OrderData(
                type, count, price,
                order.output?.orderNumber ?: throw RequestException(
                    "Amend request need order number.",
                    RequestCode.InvalidOrder
                ),
                orderAll = orderAll,
                market = market
            )
        )

    override suspend fun cancel(
        order: OrderBuy.OrderResponse,
        count: BigInteger,
        type: OrderTypeCode,
        market: MarketForOrder,
        orderAll: Boolean
    ): Result<OrderCancel.OrderResponse> =
        OrderCancel(client).call(
            OrderCancel.OrderData(
                type,
                count,
                order.output?.orderNumber ?: throw RequestException(
                    "Cancel request need order number.",
                    RequestCode.InvalidOrder
                ),
                orderAll = orderAll,
                market = market
            )
        )

    @Deprecated(
        "Use buy(count, type, market, price) instead",
        replaceWith = ReplaceWith("buy(count, type, market, price)")
    )
    override suspend fun buy(
        count: BigInteger,
        type: OrderTypeCode,
        price: BigInteger
    ): Result<OrderBuy.OrderResponse> = buy(count, type, MarketForOrder.KRX, price)

    @Deprecated(
        "Use sell(count, type, market, price) instead",
        replaceWith = ReplaceWith("sell(count, type, market, price)")
    )
    override suspend fun sell(
        count: BigInteger,
        type: OrderTypeCode,
        price: BigInteger
    ): Result<OrderBuy.OrderResponse> = sell(count, type, MarketForOrder.KRX, price)

    @Deprecated(
        "Use amend(order, count, type, market, price, orderAll) instead",
        replaceWith = ReplaceWith("amend(order, count, type, market, price, orderAll)")
    )
    override suspend fun amend(
        order: OrderBuy.OrderResponse,
        count: BigInteger,
        type: OrderTypeCode,
        price: BigInteger,
        orderAll: Boolean
    ): Result<OrderAmend.OrderResponse> = amend(order, count, type, MarketForOrder.KRX, price, orderAll)

    @Deprecated(
        "Use cancel(order, count, type, market, orderAll) instead",
        replaceWith = ReplaceWith("cancel(order, count, type, market, orderAll)")
    )
    override suspend fun cancel(
        order: OrderBuy.OrderResponse,
        count: BigInteger,
        type: OrderTypeCode,
        orderAll: Boolean
    ): Result<OrderCancel.OrderResponse> = cancel(order, count, type, MarketForOrder.KRX, orderAll)

    override suspend fun useLiveConfirmPrice(
        market: MarketWithUnified,
        block: Closeable.(InquireLivePrice.InquireLivePriceResponse) -> Unit
    ) {
        coroutineScope {
            InquireLivePrice(client).apply {
                (this@coroutineScope).launch {
                    register(InquireLivePrice.InquireLivePriceData(this@StockDomesticImpl.ticker, market)) {
                        (object : Closeable {
                            override suspend fun close() {
                                unregister(InquireLivePrice.InquireLivePriceData(this@StockDomesticImpl.ticker, market))
                            }
                        }).block(it)
                    }
                }
            }
        }
    }
}