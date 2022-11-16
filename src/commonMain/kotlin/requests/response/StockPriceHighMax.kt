package com.github.devngho.kisopenapi.requests.response

import com.github.devngho.kisopenapi.requests.Response
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

interface StockPriceHighMax : StockPriceBase, Response {
    @SerialName("stck_oprc") @Contextual val marketPrice: BigInteger?
    @SerialName("stck_hgpr") @Contextual val highPrice: BigInteger?
    @SerialName("stck_lwpr") @Contextual val lowPrice: BigInteger?
    @SerialName("acml_vol") @Contextual val accumulateTradeVolume: BigInteger?
    @SerialName("hts_frgn_ehrt") @Contextual val htsForeignerExhaustionRate: BigDecimal?
    @SerialName("frgn_ntby_qty") @Contextual val foreignerNetBuyCount: BigInteger?
    @SerialName("prdy_vrss_vol_rate") @Contextual val rateTradeVolumeFromYesterday: BigDecimal?
}