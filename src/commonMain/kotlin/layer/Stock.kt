package com.github.devngho.kisopenapi.layer

import com.github.devngho.kisopenapi.KisOpenApi
import com.github.devngho.kisopenapi.requests.InquirePrice
import com.github.devngho.kisopenapi.requests.Response
import com.github.devngho.kisopenapi.requests.response.StockPrice
import com.github.devngho.kisopenapi.requests.response.StockPriceBase
import com.github.devngho.kisopenapi.requests.response.StockPriceFull
import com.github.devngho.kisopenapi.requests.response.StockPriceHighMax
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlin.reflect.KClass


class Stock(val client: KisOpenApi, val code: String) {
    class Price {
        var price: BigInteger? = null
        var changeFromDayBefore: BigInteger? = null
        var changeRateFromDayBefore: BigDecimal? = null

        /**
         * 상한가
         */
        var maxPrice: BigInteger? = null

        /**
         * 하한가
         */
        var minPrice: BigInteger? = null
    }

    var price = Price()

    suspend fun updateBy(res: KClass<out Response>){
        when {
            res.isInstance(StockPriceFull::class) -> {
                InquirePrice(client).call(InquirePrice.InquirePriceData(code))
            }
            res.isInstance(StockPrice::class) -> {

            }
            res.isInstance(StockPriceHighMax::class) -> {

            }
            res.isInstance(StockPriceBase::class) -> {

            }
        }
    }

    private fun StockPriceBase.update() {
        this@Stock.price.also {
            it.price = price
            it.changeFromDayBefore = changeFromYesterday
            it.changeRateFromDayBefore = rateFromYesterday
        }
    }

    private fun StockPriceHighMax.update() {
        (this as StockPriceBase).update()
        this@Stock.price.also {
        }
    }

    private fun StockPrice.update() {
        (this as StockPriceHighMax).update()
        this@Stock.price.also {
            it.maxPrice = maxPrice
            it.minPrice = minPrice
        }
    }

    private fun StockPriceFull.update() {
        (this as StockPriceHighMax).update()
        this@Stock.price.also {
        }
    }
}