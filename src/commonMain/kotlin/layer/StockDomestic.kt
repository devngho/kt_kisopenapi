package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.*
import io.github.devngho.kisopenapi.requests.response.*
import io.github.devngho.kisopenapi.requests.util.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass


class StockDomestic(override val client: KisOpenApi, override val ticker: String) : IStockDomestic {
    override lateinit var price: StockPriceBase
    override var name = IStockBase.Name()
    override lateinit var tradeVolume: StockTrade
    private var liveConfirmPrice: KMutex<InquireLivePrice?> = mutex(null)

    override suspend fun updateBy(res: KClass<out Response>){
        when(res.simpleName) {
            StockPriceFull::class.simpleName,
            StockPrice::class.simpleName,
            StockPriceBase::class.simpleName,
            StockPriceChange::class.simpleName,
            StockPriceForeigner::class.simpleName,
            StockPriceHighMax::class.simpleName,
            StockTrade::class.simpleName,
            StockTradeFull::class.simpleName,
            StockTrade::class.simpleName,
            StockTradeFull::class.simpleName,
            StockTradeRate::class.simpleName,
            StockTradeAccumulate::class.simpleName -> {
                (InquirePrice(client).call(InquirePrice.InquirePriceData(ticker)).output as? StockPriceFull)?.let {
                    updateBy(it)
                }
            }
            BaseInfo::class.simpleName -> {
                ProductBaseInfo(client).call(
                    ProductBaseInfo.ProductBaseInfoData(
                        ticker,
                        ProductTypeCode.Stock
                    )
                ).output?.let {
                    updateBy(it)
                }
            }
        }
    }


    override fun updateBy(res: Response) {
        if (res is StockPriceBase) price = res
        if (res is StockTrade) tradeVolume = res
        if (res is BaseInfo) res.update()
    }

    private fun BaseInfo.update() {
        this@StockDomestic.name.also {
            it.name = name ?: it.name
            it.name120 = name120 ?: it.name120
            it.nameEng = nameEng ?: it.nameEng
            it.nameShort = nameShort ?: it.nameShort
            it.nameEng120 = nameEng120 ?: it.nameEng120
            it.nameEngShort = nameEngShort ?: it.nameEngShort
        }
    }

    override suspend fun buy(count: BigInteger, type: OrderTypeCode, price: BigInteger): OrderBuy.OrderResponse {
        if (client.account == null) throw RequestError("Buy request need account.")
        else {
            return OrderBuy(client).call(OrderBuy.OrderData(ticker, type, count, price))
        }
    }

    override suspend fun sell(count: BigInteger, type: OrderTypeCode, price: BigInteger): OrderBuy.OrderResponse {
        if (client.account == null) throw RequestError("Sell request need account.")
        else {
            return OrderSell(client).call(OrderBuy.OrderData(ticker, type, count, price))
        }
    }

    override suspend fun useLiveConfirmPrice(block: Closeable.(InquireLivePrice.InquireLivePriceResponse) -> Unit) {
        runBlocking {
            liveConfirmPrice.setIfNull {
                InquireLivePrice(client).apply {
                    (this@runBlocking).launch {
                        register(InquireLivePrice.InquireLivePriceData(this@StockDomestic.ticker)) {
                        (object : Closeable {
                            override suspend fun close() {
                                unregister(InquireLivePrice.InquireLivePriceData(this@StockDomestic.ticker))
                            }
                        }).block(it)
                    } }
                }
            }
        }
    }
}