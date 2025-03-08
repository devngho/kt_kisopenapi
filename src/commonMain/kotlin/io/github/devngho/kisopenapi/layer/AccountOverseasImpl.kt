package io.github.devngho.kisopenapi.layer

import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.overseas.inquire.InquireOverseasBalance
import io.github.devngho.kisopenapi.requests.overseas.inquire.live.InquireOverseasLiveConfirm
import io.github.devngho.kisopenapi.requests.response.balance.overseas.BalanceAccountOverseas
import io.github.devngho.kisopenapi.requests.response.balance.overseas.BalanceAccountStockOverseas
import io.github.devngho.kisopenapi.requests.response.balance.overseas.UpdatableBalanceAccountOverseas
import io.github.devngho.kisopenapi.requests.response.balance.overseas.UpdatableBalanceAccountStockOverseas
import io.github.devngho.kisopenapi.requests.util.Closeable
import io.github.devngho.kisopenapi.requests.util.Currency
import io.github.devngho.kisopenapi.requests.util.InternalApi
import io.github.devngho.kisopenapi.requests.util.OverseasMarket
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.reflect.KClass

/**
 * 해외 계좌 정보를 불러오고 관리합니다.
 *
 * @param client KisOpenApi
 * @param exchange 거래소
 * @param currency 통화
 */
@OptIn(InternalApi::class)
class AccountOverseasImpl(
    val client: KISApiClient,
    val exchange: OverseasMarket,
    val currency: Currency,
    val updatable: UpdatableBalanceAccountOverseas = UpdatableBalanceAccountOverseas()
) :
    AccountOverseas, BalanceAccountOverseas by updatable {
    override val accountStocks: MutableList<BalanceAccountStockOverseas> = mutableListOf()

    override suspend fun update(vararg type: KClass<out Response>): Unit = coroutineScope {
        type.map { async { updateSingle(it) } }.awaitAll()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun updateSingle(type: KClass<out Response>) {
        when (type) {
            BalanceAccountOverseas::class -> {
                InquireOverseasBalance(client).call(InquireOverseasBalance.InquireBalanceData(exchange, currency))
                    .getOrNull()?.run {
                        output1?.update()
                        output2?.let { updateBy(it) }
                    }
            }
        }
    }

    override fun updateBy(res: Response) {
        if (res is InquireOverseasBalance.InquireBalanceResponse) {
            res.output1?.update()
            res.output2?.let { updateBy(it) }
        } else {
            updatable.broadcast(res)
        }
    }

    @OptIn(InternalApi::class)
    private fun List<BalanceAccountStockOverseas>.update() {
        this@AccountOverseasImpl.accountStocks.also { t ->
            val removed = t.filter { it !in this }
            val added = this.filter { it !in t }
            val updated = this.filter { it in t }

            t.removeAll(removed)
            t.addAll(added.map { UpdatableBalanceAccountStockOverseas().apply { broadcast(it) } })
            updated.forEach { (t.first { it == it } as UpdatableBalanceAccountStockOverseas).broadcast(it) }
        }
    }

    override suspend fun useLiveConfirm(block: Closeable.(InquireOverseasLiveConfirm.InquireOverseasLiveConfirmResponse) -> Unit) {
        coroutineScope {
            InquireOverseasLiveConfirm(client).apply {
                (this@coroutineScope).launch {
                    register(InquireOverseasLiveConfirm.InquireOverseasLiveConfirmData()) {
                        (object : Closeable {
                            override suspend fun close() {
                                unregister(InquireOverseasLiveConfirm.InquireOverseasLiveConfirmData())
                            }
                        }).block(it)
                    }
                }
            }
        }
    }
}