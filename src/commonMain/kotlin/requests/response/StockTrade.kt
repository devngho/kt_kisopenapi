package io.github.devngho.kisopenapi.requests.response

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.requests.Response
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

/**
 * [KIS Developers 문서](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-quotations)를 참조하세요.
 * @see io.github.devngho.kisopenapi.requests.InquirePrice
 */
interface StockTrade: Response {
    /** 전일 대비 거래량 비율 */
    @SerialName("prdy_vrss_vol_rate") @Contextual val rateTradeVolumeFromYesterday: BigDecimal?

    /** 누적 거래량 */
    @SerialName("acml_vol") @Contextual val accumulateTradeVolume: BigInteger?
}