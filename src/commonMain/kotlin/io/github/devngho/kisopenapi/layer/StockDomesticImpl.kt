package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.common.InquireProductBaseInfo
import io.github.devngho.kisopenapi.requests.domestic.inquire.InquirePrice
import io.github.devngho.kisopenapi.requests.domestic.inquire.live.InquireLivePrice
import io.github.devngho.kisopenapi.requests.domestic.order.OrderAmend
import io.github.devngho.kisopenapi.requests.domestic.order.OrderBuy
import io.github.devngho.kisopenapi.requests.domestic.order.OrderCancel
import io.github.devngho.kisopenapi.requests.domestic.order.OrderSell
import io.github.devngho.kisopenapi.requests.response.stock.BaseProductInfo
import io.github.devngho.kisopenapi.requests.response.stock.ProductInfo
import io.github.devngho.kisopenapi.requests.response.stock.StockInfo
import io.github.devngho.kisopenapi.requests.response.stock.price.domestic.*
import io.github.devngho.kisopenapi.requests.response.stock.trade.StockTrade
import io.github.devngho.kisopenapi.requests.response.stock.trade.StockTradeAccumulate
import io.github.devngho.kisopenapi.requests.response.stock.trade.StockTradeFull
import io.github.devngho.kisopenapi.requests.response.stock.trade.StockTradeRate
import io.github.devngho.kisopenapi.requests.util.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.reflect.KClass


/**
 * 국내 주식 정보를 불러오고 관리합니다.
 * @param client KisOpenApi
 * @param ticker 종목 코드
 */
class StockDomesticImpl(override val client: KISApiClient, override val ticker: String) : StockDomestic {
    override lateinit var price: StockPriceBase
    override var name = StockBase.Name()
    override lateinit var tradeVolume: StockTrade


    override suspend fun update(vararg type: KClass<out Response>): Unit = coroutineScope {
        type.map { async { updateSingle(it) } }.awaitAll()
    }

    @OptIn(DemoNotSupported::class)
    private suspend fun updateSingle(type: KClass<out Response>) {
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
                (InquirePrice(client).call(InquirePrice.InquirePriceData(ticker))
                    .getOrThrow().output as StockPriceFull).let {
                    updateBy(it)
                }
            }

            BaseProductInfo::class,
            ProductInfo::class,
            StockInfo::class -> {
                InquireProductBaseInfo(client).call(
                    InquireProductBaseInfo.InquireProductBaseInfoData(
                        ticker,
                        ProductTypeCode.Stock
                    )
                ).getOrThrow().output!!.let {
                    updateBy(it)
                }
            }
        }
    }


    override fun updateBy(res: Response) {
        if (res is StockPriceBase) price = res
        if (res is StockTrade) tradeVolume = res
        if (res is ProductInfo) res.update()
    }

    private fun ProductInfo.update() {
        this@StockDomesticImpl.name.also {
            it.name = name ?: it.name
            it.name120 = name120 ?: it.name120
            it.nameEnglish = nameEng ?: it.nameEnglish
            it.nameShort = nameShort ?: it.nameShort
            it.nameEng120 = nameEng120 ?: it.nameEng120
            it.nameEnglishShort = nameEngShort ?: it.nameEnglishShort
        }
    }

    override suspend fun buy(
        count: BigInteger,
        type: OrderTypeCode,
        price: BigInteger
    ): Result<OrderBuy.OrderResponse> = OrderBuy(client).call(OrderBuy.OrderData(ticker, type, count, price))

    override suspend fun sell(
        count: BigInteger,
        type: OrderTypeCode,
        price: BigInteger
    ): Result<OrderBuy.OrderResponse> = OrderSell(client).call(OrderBuy.OrderData(ticker, type, count, price))

    override suspend fun amend(
        order: OrderBuy.OrderResponse,
        count: BigInteger,
        type: OrderTypeCode,
        price: BigInteger,
        orderAll: Boolean
    ): Result<OrderAmend.OrderResponse> =
        OrderAmend(client).call(
            OrderAmend.OrderData(
                type, count, price,
                order.output?.orderNumber ?: throw RequestException(
                    "Amend request need order number.",
                    RequestCode.InvalidOrder
                ), orderAll
            )
        )

    override suspend fun cancel(
        order: OrderBuy.OrderResponse,
        count: BigInteger,
        type: OrderTypeCode,
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
                orderAll
            )
        )

    override suspend fun useLiveConfirmPrice(block: Closeable.(InquireLivePrice.InquireLivePriceResponse) -> Unit) {
        coroutineScope {
            InquireLivePrice(client).apply {
                (this@coroutineScope).launch {
                    register(InquireLivePrice.InquireLivePriceData(this@StockDomesticImpl.ticker)) {
                        (object : Closeable {
                            override suspend fun close() {
                                unregister(InquireLivePrice.InquireLivePriceData(this@StockDomesticImpl.ticker))
                            }
                        }).block(it)
                    }
                }
            }
        }
    }
}