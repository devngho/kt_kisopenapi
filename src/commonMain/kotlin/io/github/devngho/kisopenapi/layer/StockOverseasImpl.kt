package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.common.InquireProductBaseInfo
import io.github.devngho.kisopenapi.requests.overseas.inquire.InquireOverseasDetailedPrice
import io.github.devngho.kisopenapi.requests.overseas.inquire.InquireOverseasPrice
import io.github.devngho.kisopenapi.requests.overseas.inquire.live.InquireOverseasLivePrice
import io.github.devngho.kisopenapi.requests.overseas.order.OrderOverseasAmend
import io.github.devngho.kisopenapi.requests.overseas.order.OrderOverseasBuy
import io.github.devngho.kisopenapi.requests.overseas.order.OrderOverseasCancel
import io.github.devngho.kisopenapi.requests.overseas.order.OrderOverseasSell
import io.github.devngho.kisopenapi.requests.response.stock.BaseInfo
import io.github.devngho.kisopenapi.requests.response.stock.price.overseas.StockOverseasPrice
import io.github.devngho.kisopenapi.requests.response.stock.price.overseas.StockOverseasPriceBase
import io.github.devngho.kisopenapi.requests.response.stock.price.overseas.StockOverseasPriceFull
import io.github.devngho.kisopenapi.requests.util.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.reflect.KClass


/**
 * 해외 주식 정보를 불러오고 관리합니다.
 * @param client KisOpenApi
 * @param ticker 종목 코드
 * @param market 거래소 코드
 */
class StockOverseasImpl(
    override val client: KISApiClient,
    override val ticker: String,
    override val market: OverseasMarket
) :
    StockOverseas {
    override lateinit var price: StockOverseasPriceBase
    override var name = StockBase.Name()

    @OptIn(DemoNotSupported::class)
    override suspend fun update(res: KClass<out Response>) {
        when (res) {
            StockOverseasPrice::class,
            StockOverseasPriceBase::class -> {
                (InquireOverseasPrice(client).call(
                    InquireOverseasPrice.InquirePriceData(
                        ticker,
                        market
                    )
                ).getOrThrow().output as StockOverseasPrice).let {
                    updateBy(it)
                }
            }
            StockOverseasPriceFull::class -> {
                (InquireOverseasDetailedPrice(client).call(
                    InquireOverseasDetailedPrice.InquirePriceData(
                        ticker,
                        market
                    )
                ).getOrThrow().output as StockOverseasPriceFull).let {
                    updateBy(it)
                }
            }

            BaseInfo::class -> {
                val type = when (market) {
                    OverseasMarket.NASDAQ, OverseasMarket.NAS -> ProductTypeCode.Nasdaq
                    OverseasMarket.NEWYORK, OverseasMarket.NYS -> ProductTypeCode.NewYork
                    OverseasMarket.AMEX, OverseasMarket.AMS -> ProductTypeCode.Amex
                    OverseasMarket.TOKYO, OverseasMarket.TSE -> ProductTypeCode.Japan
                    OverseasMarket.HONGKONG, OverseasMarket.HKS -> ProductTypeCode.HongKong
                    OverseasMarket.HANOI, OverseasMarket.HNX -> ProductTypeCode.VietnamHanoi
                    OverseasMarket.HOCHIMINH, OverseasMarket.HSX -> ProductTypeCode.VietnamHoChiMinh
                    OverseasMarket.SHANGHAI, OverseasMarket.SHS -> ProductTypeCode.ChinaSanghaeA
                    OverseasMarket.SHENZHEN, OverseasMarket.SZS -> ProductTypeCode.ChinaSimCheonA
                    else -> ProductTypeCode.Stock
                }
                InquireProductBaseInfo(client).call(InquireProductBaseInfo.InquireProductBaseInfoData(ticker, type))
                    .getOrNull()?.output?.let {
                        updateBy(it)
                    }
            }
        }
    }


    override fun updateBy(res: Response) {
        if (res is StockOverseasPrice) price = res
        if (res is BaseInfo) res.update()
    }

    private fun BaseInfo.update() {
        this@StockOverseasImpl.name.also {
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
        price: BigDecimal
    ): Result<OrderOverseasBuy.OrderResponse> =
        OrderOverseasBuy(client).call(OrderOverseasBuy.OrderData(ticker, market, type, count, price))

    override suspend fun sell(
        count: BigInteger,
        type: OrderTypeCode,
        price: BigDecimal
    ): Result<OrderOverseasBuy.OrderResponse> =
        OrderOverseasSell(client).call(OrderOverseasBuy.OrderData(ticker, market, type, count, price))

    override suspend fun amend(
        order: OrderOverseasBuy.OrderResponse,
        count: BigInteger,
        type: OrderTypeCode,
        price: BigDecimal
    ): Result<OrderOverseasAmend.OrderResponse> =
        OrderOverseasAmend(client).call(
            OrderOverseasAmend.OrderData(
                ticker,
                market,
                count,
                price,
                order.output?.orderNumber ?: throw RequestException(
                    "Amend request need order number.",
                    RequestCode.InvalidOrder
                )
            )
        )

    override suspend fun cancel(
        order: OrderOverseasBuy.OrderResponse,
        count: BigInteger,
        type: OrderTypeCode
    ): Result<OrderOverseasCancel.OrderResponse> =
        OrderOverseasCancel(client).call(
            OrderOverseasCancel.OrderData(
                ticker,
                market,
                count,
                order.output?.orderNumber ?: throw RequestException(
                    "Cancel request need order number.",
                    RequestCode.InvalidOrder
                )
            )
        )

    override suspend fun useLiveConfirmPrice(block: Closeable.(InquireOverseasLivePrice.InquireOverseasLivePriceResponse) -> Unit): Unit =
        coroutineScope {
            InquireOverseasLivePrice(client).apply {
                launch {
                    register(
                        InquireOverseasLivePrice.InquireOverseasLivePriceData(
                            this@StockOverseasImpl.ticker,
                            market
                        )
                    ) {
                        updateBy(it)
                        (object : Closeable {
                            override suspend fun close() {
                                unregister(
                                    InquireOverseasLivePrice.InquireOverseasLivePriceData(
                                        this@StockOverseasImpl.ticker,
                                        market
                                    )
                                )
                            }
                        }).block(it)
                    }
                }
            }
        }
}