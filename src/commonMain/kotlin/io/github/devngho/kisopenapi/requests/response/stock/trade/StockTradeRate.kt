package io.github.devngho.kisopenapi.requests.response.stock.trade

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

/**
 * [KIS Developers 문서](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-quotations)를 참조하세요.
 * @see io.github.devngho.kisopenapi.requests.domestic.inquire.InquirePrice
 */
@Suppress("SpellCheckingInspection")
interface StockTradeRate : StockTrade {
    /** 거래량 회전율 */
    @SerialName("vol_tnrt")
    @Contextual
    val tradeVolumeTurningRate: BigDecimal?
}