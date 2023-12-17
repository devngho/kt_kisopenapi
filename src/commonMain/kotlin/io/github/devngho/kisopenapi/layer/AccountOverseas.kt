package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.overseas.inquire.live.InquireOverseasLiveConfirm
import io.github.devngho.kisopenapi.requests.util.Closeable
import io.github.devngho.kisopenapi.requests.util.Currency
import io.github.devngho.kisopenapi.requests.util.OverseasMarket

interface AccountOverseas : Account {
    val assetAmount: BigDecimal?
    val evalAmount: BigDecimal?
    override val accountStocks: MutableList<AccountStock>

    suspend fun useLiveConfirm(block: Closeable.(InquireOverseasLiveConfirm.InquireOverseasLiveConfirmResponse) -> Unit)

    companion object {
        fun create(
            client: KISApiClient,
            currency: Currency,
            exchange: OverseasMarket
        ): AccountOverseas = AccountOverseasImpl(client, exchange, currency)
    }
}