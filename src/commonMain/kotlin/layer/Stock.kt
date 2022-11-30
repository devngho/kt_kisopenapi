package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.*
import io.github.devngho.kisopenapi.requests.response.*
import io.github.devngho.kisopenapi.requests.util.OrderTypeCode
import io.github.devngho.kisopenapi.requests.util.ProductTypeCode
import io.github.devngho.kisopenapi.requests.util.RequestError
import kotlin.reflect.KClass


class Stock(override val client: KisOpenApi, override val code: String) : IStock{
    override var price = IStock.Price()
    override var name = IStock.Name()
    override var tradeVolume = IStock.TradeVolume()

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
                (InquirePrice(client).call(InquirePrice.InquirePriceData(code)).output as? StockPriceFull)?.let {
                    updateBy(it)
                }
            }
            BaseInfo::class.simpleName -> {
                ProductBaseInfo(client).call(ProductBaseInfo.ProductBaseInfoData(code, ProductTypeCode.Stock)).output?.let {
                    updateBy(it)
                }
            }
        }
    }

    override fun updateBy(res: Response) {
        if (res is StockPriceBase) res.update()
        if (res is StockPrice) res.update()
        if (res is StockPriceChange) res.update()
        if (res is StockPriceForeigner) res.update()
        if (res is StockPriceFull) res.update()
        if (res is StockPriceHighMax) res.update()
        if (res is StockTrade) res.update()
        if (res is StockTradeAccumulate) res.update()
        if (res is StockTradeRate) res.update()
        if (res is StockTradeFull) res.update()
        if (res is BaseInfo) res.update()
    }

    private fun BaseInfo.update() {
        this@Stock.name.also {
            it.name = name ?: it.name
            it.name120 = name120 ?: it.name120
            it.nameEng = nameEng ?: it.nameEng
            it.nameShort = nameShort ?: it.nameShort
            it.nameEng120 = nameEng120 ?: it.nameEng120
            it.nameEngShort = nameEngShort ?: it.nameEngShort
        }
    }

    private fun StockPriceBase.update() {
        this@Stock.price.also {
            it.price = price ?: it.price
        }
    }

    private fun StockPriceChange.update() {
        this@Stock.price.also {
            it.changeFromDayBefore = changeFromYesterday ?: it.changeFromDayBefore
            it.changeRateFromDayBefore = rateFromYesterday ?: it.changeRateFromDayBefore
        }
    }

    private fun StockPriceForeigner.update() {
        this@Stock.price.also {
        }
    }

    private fun StockPriceHighMax.update() {
        this@Stock.price.also {
            it.highPrice = highPrice ?: it.highPrice
            it.lowPrice = lowPrice ?: it.lowPrice
        }
    }

    private fun StockPrice.update() {
        this@Stock.price.also {
            it.maxPrice = maxPrice ?: it.maxPrice
            it.minPrice = minPrice ?: it.minPrice
        }
    }

    private fun StockPriceFull.update() {
        this@Stock.price.also {
        }
    }

    private fun StockTrade.update() {
        this@Stock.tradeVolume.also {
            it.volumeAccumulate = accumulateTradeVolume ?: it.volumeAccumulate
            it.volumeRateFromYesterday = rateTradeVolumeFromYesterday ?: it.volumeRateFromYesterday
        }
    }

    private fun StockTradeAccumulate.update() {
        this@Stock.tradeVolume.also {
        }
    }
    private fun StockTradeRate.update() {
        this@Stock.tradeVolume.also {
        }
    }
    private fun StockTradeFull.update() {
        this@Stock.tradeVolume.also {
        }
    }

    override suspend fun buy(count: BigInteger, type: OrderTypeCode, price: BigInteger): OrderBuy.OrderResponse {
        if (client.account == null) throw RequestError("Buy request need account.")
        else {
            return OrderBuy(client).call(OrderBuy.OrderData(code, type, count, price))
        }
    }

    override suspend fun sell(count: BigInteger, type: OrderTypeCode, price: BigInteger): OrderBuy.OrderResponse {
        if (client.account == null) throw RequestError("Buy request need account.")
        else {
            return OrderSell(client).call(OrderBuy.OrderData(code, type, count, price))
        }
    }
}