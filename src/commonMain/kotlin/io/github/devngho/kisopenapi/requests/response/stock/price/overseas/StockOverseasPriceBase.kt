package io.github.devngho.kisopenapi.requests.response.stock.price.overseas

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import io.github.devngho.kisopenapi.requests.Response
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

/**
 * [KIS Developers 문서](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-current)를 참조하세요.
 * @see io.github.devngho.kisopenapi.requests.overseas.inquire.InquireOverseasPrice
 */
interface StockOverseasPriceBase : Response {
    /** 가격 */
    @SerialName("last")
    @Contextual
    val price: BigDecimal?
}