package io.github.devngho.kisopenapi.requests.response.stock.price.domestic

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.requests.Response
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

/**
 * [KIS Developers 문서](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-quotations)를 참조하세요.
 * @see io.github.devngho.kisopenapi.requests.domestic.inquire.InquirePrice
 */
@Suppress("SpellCheckingInspection")
interface StockPrice : StockPriceLowHigh, Response {
    /** 최고가 */
    @SerialName("stck_mxpr")
    @Contextual
    val maxPrice: BigInteger?

    /** 최저가 */
    @SerialName("stck_llam")
    @Contextual
    val minPrice: BigInteger?
}