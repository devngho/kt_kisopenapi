package io.github.devngho.kisopenapi.layer

import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.domestic.inquire.InquireBalance
import io.github.devngho.kisopenapi.requests.domestic.inquire.live.InquireLiveConfirm
import io.github.devngho.kisopenapi.requests.response.balance.domestic.BalanceAccount
import io.github.devngho.kisopenapi.requests.response.balance.domestic.BalanceAccountStock
import io.github.devngho.kisopenapi.requests.response.balance.domestic.UpdatableBalanceAccount
import io.github.devngho.kisopenapi.requests.response.balance.domestic.UpdatableBalanceAccountStock
import io.github.devngho.kisopenapi.requests.util.Closeable
import io.github.devngho.kisopenapi.requests.util.InternalApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

@OptIn(InternalApi::class)
class AccountDomesticImpl(
    val client: KISApiClient,
    val updatable: UpdatableBalanceAccount = UpdatableBalanceAccount()
) : AccountDomestic, BalanceAccount by updatable {
    override val accountStocks: MutableList<BalanceAccountStock> = mutableListOf()

    override suspend fun update(vararg type: KClass<out Response>): Unit = coroutineScope {
        type.map { async { updateSingle(it) } }.awaitAll()
    }

    private suspend fun updateSingle(type: KClass<out Response>) {
        when (type) {
            BalanceAccount::class -> {
                InquireBalance(client).call(
                    InquireBalance.InquireBalanceData(
                        afterHourFinalPrice = true, includeFund = true,
                        includeYesterdayTrade = false
                    )
                ).getOrNull()?.run {
                    output1?.update()
                    output2?.let {
                        updateBy(it[0])
                    }
                }
            }
        }
    }

    override fun updateBy(res: Response) {
        if (res is InquireBalance.InquireBalanceResponse) {
            res.output1?.update()
            res.output2?.let {
                updateBy(it[0])
            }
        } else {
            updatable.broadcast(res)
        }
    }

    @OptIn(InternalApi::class)
    private fun List<BalanceAccountStock>.update() {
        this@AccountDomesticImpl.accountStocks.also { t ->
            val removed = t.filter { it !in this }
            val added = this.filter { it !in t }
            val updated = this.filter { it in t }

            t.removeAll(removed)
            t.addAll(added.map { UpdatableBalanceAccountStock().apply { broadcast(it) } })
            updated.forEach { (t.first { it == it } as UpdatableBalanceAccountStock).broadcast(it) }
        }
    }

    override suspend fun useLiveConfirm(block: Closeable.(InquireLiveConfirm.InquireLiveConfirmResponse) -> Unit) {
        coroutineScope {
            InquireLiveConfirm(client).apply {
                (this@coroutineScope).launch {
                    register(InquireLiveConfirm.InquireLiveConfirmData()) {
                        (object : Closeable {
                            override suspend fun close() {
                                unregister(InquireLiveConfirm.InquireLiveConfirmData())
                            }
                        }).block(it)
                    }
                }
            }
        }
    }
}