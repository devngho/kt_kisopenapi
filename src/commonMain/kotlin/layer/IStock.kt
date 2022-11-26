package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.OrderBuy
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.util.OrderTypeCode
import kotlin.reflect.KClass


interface IStock {
    val client: KisOpenApi
    val code: String
    /**
     * @param price 현재가
     * @param changeFromDayBefore 전일 대비 변화
     * @param changeRateFromDayBefore 전일 대비 변화율
     * @param maxPrice 상한가
     * @param minPrice 하한가
     * @param highPrice 최고가
     * @param lowPrice 최저가
     */
    data class Price(
        var price: BigInteger? = null,
        var changeFromDayBefore: BigInteger? = null,
        var changeRateFromDayBefore: BigDecimal? = null,
        var maxPrice: BigInteger? = null,
        var minPrice: BigInteger? = null,
        var lowPrice: BigInteger? = null,
        var highPrice: BigInteger? = null
    )

    data class Name(
        var name: String? = null,
        var name120: String? = null,
        var nameEng: String? = null,
        var nameEng120: String? = null,
        var nameShort: String? = null,
        var nameEngShort: String? = null
    )

    data class TradeVolume(
        var volumeRateFromYesterday: BigDecimal? = null,
        var volumeAccumulate: BigInteger? = null,
    )

    var price: Price
    var name: Name
    var tradeVolume: TradeVolume

    suspend fun updateBy(res: KClass<out Response>)
    fun updateBy(res: Response)

    suspend fun buy(count: BigInteger, type: OrderTypeCode, price: BigInteger = BigInteger(0)): OrderBuy.OrderResponse
    suspend fun sell(count: BigInteger, type: OrderTypeCode, price: BigInteger = BigInteger(0)): OrderBuy.OrderResponse
}