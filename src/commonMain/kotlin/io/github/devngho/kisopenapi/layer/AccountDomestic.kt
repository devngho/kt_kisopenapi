package io.github.devngho.kisopenapi.layer

import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.domestic.inquire.live.InquireLiveConfirm
import io.github.devngho.kisopenapi.requests.response.balance.domestic.BalanceAccount
import io.github.devngho.kisopenapi.requests.response.balance.domestic.BalanceAccountStock
import io.github.devngho.kisopenapi.requests.util.Closeable
import io.github.devngho.kisopenapi.requests.util.InternalApi
import kotlin.jvm.JvmStatic

/**
 * 국내 계좌 정보를 불러오고 관리합니다.
 */
interface AccountDomestic : Account, BalanceAccount {
    val accountStocks: MutableList<BalanceAccountStock>

    suspend fun useLiveConfirm(block: Closeable.(InquireLiveConfirm.InquireLiveConfirmResponse) -> Unit)

    companion object {
        @JvmStatic
        fun create(
            client: KISApiClient,
        ): AccountDomestic = @OptIn(InternalApi::class) AccountDomesticImpl(client)
    }
}