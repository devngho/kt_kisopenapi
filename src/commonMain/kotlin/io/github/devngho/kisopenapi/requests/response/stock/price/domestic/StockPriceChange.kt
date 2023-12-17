package io.github.devngho.kisopenapi.requests.response.stock.price.domestic

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.requests.util.SignPrice
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

/**
 * [KIS Developers 문서](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-quotations)를 참조하세요.
 * @see io.github.devngho.kisopenapi.requests.domestic.inquire.InquirePrice
 */
interface StockPriceChange {
    /** 전일 대비 가격 변동 */
    @SerialName("prdy_vrss")
    @Contextual
    val changeFromYesterday: BigInteger?

    /** 전일 대비 가격 부호 */
    @SerialName("prdy_vrss_sign")
    val signFromYesterday: SignPrice?

    /** 전일 대비 등락률 */
    @SerialName("prdy_ctrt")
    @Contextual
    val rateFromYesterday: BigDecimal?
}