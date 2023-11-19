package io.github.devngho.kisopenapi.requests.response

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

/**
 * [KIS Developers 문서](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-quotations)를 참조하세요.
 * @see io.github.devngho.kisopenapi.requests.InquirePrice
 */
interface StockPriceForeigner: StockPriceBase {
    /** HTS 외국인 소진률 */
    @SerialName("hts_frgn_ehrt") @Contextual val htsForeignerExhaustionRate: BigDecimal?

    /** 외국인 순매수 수량 */
    @SerialName("frgn_ntby_qty") @Contextual val foreignerNetBuyCount: BigInteger?
}