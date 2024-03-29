package io.github.devngho.kisopenapi.requests.response.stock.price.domestic

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

/**
 * [KIS Developers 문서](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-quotations)를 참조하세요.
 * @see io.github.devngho.kisopenapi.requests.domestic.inquire.InquirePrice
 */
@Suppress("SpellCheckingInspection")
interface StockPriceBase {
    /** 가격 */
    @SerialName("stck_prpr")
    @Contextual
    val price: BigInteger?
}