package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.domestic.inquire.live.InquireLiveConfirm
import io.github.devngho.kisopenapi.requests.util.Closeable
import kotlin.jvm.JvmStatic

interface AccountDomestic : Account {
    val assetAmount: BigInteger?
    val evalAmount: BigInteger?
    override val accountStocks: MutableList<AccountStock>

    suspend fun useLiveConfirm(block: Closeable.(InquireLiveConfirm.InquireLiveConfirmResponse) -> Unit)

    companion object {
        @JvmStatic
        fun create(
            client: KISApiClient,
        ): AccountDomestic = AccountDomesticImpl(client)
    }
}