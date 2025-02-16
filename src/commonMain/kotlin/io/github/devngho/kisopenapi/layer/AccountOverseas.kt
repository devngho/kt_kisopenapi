package io.github.devngho.kisopenapi.layer

import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.overseas.inquire.live.InquireOverseasLiveConfirm
import io.github.devngho.kisopenapi.requests.response.balance.overseas.BalanceAccountOverseas
import io.github.devngho.kisopenapi.requests.response.balance.overseas.BalanceAccountStockOverseas
import io.github.devngho.kisopenapi.requests.util.Closeable
import io.github.devngho.kisopenapi.requests.util.Currency
import io.github.devngho.kisopenapi.requests.util.InternalApi
import io.github.devngho.kisopenapi.requests.util.OverseasMarket

interface AccountOverseas : Account, BalanceAccountOverseas {
    val accountStocks: MutableList<BalanceAccountStockOverseas>

    suspend fun useLiveConfirm(block: Closeable.(InquireOverseasLiveConfirm.InquireOverseasLiveConfirmResponse) -> Unit)

    companion object {
        fun create(
            client: KISApiClient,
            currency: Currency,
            exchange: OverseasMarket
        ): AccountOverseas = @OptIn(InternalApi::class) AccountOverseasImpl(client, exchange, currency)
    }
}