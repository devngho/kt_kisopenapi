package io.github.devngho.kisopenapi.requests.response.stock.price.overseas

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.requests.util.SignPrice
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

/**
 * [KIS Developers 문서](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-current)를 참조하세요.
 * @see io.github.devngho.kisopenapi.requests.overseas.inquire.InquireOverseasPrice
 */
interface StockOverseasPrice : StockOverseasPriceBase {
    /** 소수점 위치 */
    @SerialName("zdiv")
    val decimalPoint: Int?

    @SerialName("last")
    @Contextual
    override val price: BigDecimal?

    /** 부호 */
    @SerialName("sign")
    val sign: SignPrice?

    /** 전일 대비 가격 */
    @SerialName("diff")
    @Contextual
    val changeFromYesterday: BigDecimal?

    /** 전일 대비 등락률 */
    @SerialName("rate")
    @Contextual
    val rateFromYesterday: BigDecimal?

    /** 거래량 */
    @SerialName("tvol")
    @Contextual
    val tradeVolume: BigInteger?

    /** 거래 대금 */
    @SerialName("tamt")
    @Contextual
    val tradePriceVolume: BigDecimal?
}