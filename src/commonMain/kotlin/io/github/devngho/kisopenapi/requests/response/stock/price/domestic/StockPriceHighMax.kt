package io.github.devngho.kisopenapi.requests.response.stock.price.domestic

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.requests.Response
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

/**
 * [KIS Developers 문서](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-quotations)를 참조하세요.
 * @see io.github.devngho.kisopenapi.requests.domestic.inquire.InquirePrice
 */
interface StockPriceHighMax : StockPriceBase, Response {
    /** 시가 */
    @SerialName("stck_oprc")
    @Contextual
    val openingPrice: BigInteger?

    /** 고가 */
    @SerialName("stck_hgpr")
    @Contextual
    val highPrice: BigInteger?

    /** 저가 */
    @SerialName("stck_lwpr")
    @Contextual
    val lowPrice: BigInteger?
}