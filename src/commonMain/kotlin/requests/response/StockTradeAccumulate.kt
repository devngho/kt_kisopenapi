package io.github.devngho.kisopenapi.requests.response

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

/**
 * [KIS Developers 문서](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-quotations)를 참조하세요.
 * @see io.github.devngho.kisopenapi.requests.InquirePrice
 */
interface StockTradeAccumulate: StockTrade {
    /** 누적 거래 대금 */
    @SerialName("acml_tr_pbmn") @Contextual val accumulateTradePrice: BigInteger?
}