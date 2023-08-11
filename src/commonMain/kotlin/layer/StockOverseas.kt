package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.*
import io.github.devngho.kisopenapi.requests.response.*
import io.github.devngho.kisopenapi.requests.util.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass


class StockOverseas(override val client: KisOpenApi, override val code: String, override val market: OverseasMarket) : IStockOverseas{
    override lateinit var price: StockOverseasPrice
    override var name = IStockBase.Name()
    private var liveConfirmPrice: KMutex<InquireOverseasLivePrice?> = mutex(null)

    override suspend fun updateBy(res: KClass<out Response>){
        when(res.simpleName) {
            StockOverseasPrice::class.simpleName -> {
                (InquireOverseasPrice(client).call(InquireOverseasPrice.InquirePriceData(code, market)).output as? StockOverseasPrice)?.let {
                    updateBy(it)
                }
            }
            BaseInfo::class.simpleName -> {
                val type = when(market) {
                    OverseasMarket.NASDAQ, OverseasMarket.NAS -> ProductTypeCode.Nasdaq
                    OverseasMarket.NEWYORK, OverseasMarket.NYS -> ProductTypeCode.NewYork
                    OverseasMarket.AMEX, OverseasMarket.AMS -> ProductTypeCode.Amex
                    OverseasMarket.TOYKO, OverseasMarket.TSE -> ProductTypeCode.Japan
                    OverseasMarket.HONGKONG, OverseasMarket.HKS -> ProductTypeCode.HongKong
                    OverseasMarket.HANOI, OverseasMarket.HNX -> ProductTypeCode.VietnamHanoi
                    OverseasMarket.HOCHIMINH, OverseasMarket.HSX -> ProductTypeCode.VietnamHoChiMinh
                    OverseasMarket.SHANGHAI, OverseasMarket.SHS -> ProductTypeCode.ChinaSanghaeA
                    OverseasMarket.SHENZHEN, OverseasMarket.SZS -> ProductTypeCode.ChinaSimCheonA
                    else -> ProductTypeCode.Stock
                }
                ProductBaseInfo(client).call(ProductBaseInfo.ProductBaseInfoData(code, type)).output?.let {
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
        this@StockOverseas.name.also {
            it.name = name ?: it.name
            it.name120 = name120 ?: it.name120
            it.nameEng = nameEng ?: it.nameEng
            it.nameShort = nameShort ?: it.nameShort
            it.nameEng120 = nameEng120 ?: it.nameEng120
            it.nameEngShort = nameEngShort ?: it.nameEngShort
        }
    }

    override suspend fun buy(count: BigInteger, type: OrderTypeCode, price: BigDecimal): OrderOverseasBuy.OrderResponse {
        if (client.account == null) throw RequestError("Buy request need account.")
        else {
            return OrderOverseasBuy(client).call(OrderOverseasBuy.OrderData(code, market, type, count, price))
        }
    }

    override suspend fun sell(count: BigInteger, type: OrderTypeCode, price: BigDecimal): OrderOverseasBuy.OrderResponse {
        if (client.account == null) throw RequestError("Sell request need account.")
        else {
            return OrderOverseasSell(client).call(OrderOverseasBuy.OrderData(code, market, type, count, price))
        }
    }

    override suspend fun useLiveConfirmPrice(block: Closeable.(InquireOverseasLivePrice.InquireLivePriceResponse) -> Unit) {
        runBlocking {
            liveConfirmPrice.setIfNull {
                InquireOverseasLivePrice(client).apply {
                    (this@runBlocking).launch { register(InquireOverseasLivePrice.InquireLivePriceData(this@StockOverseas.code, market)) {
                        (object : Closeable {
                            override suspend fun close() {
                                unregister(InquireOverseasLivePrice.InquireLivePriceData(this@StockOverseas.code, market))
                            }
                        }).block(it)
                    } }
                }
            }
        }
    }
}