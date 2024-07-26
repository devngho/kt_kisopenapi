package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.overseas.inquire.InquireOverseasBalance
import io.github.devngho.kisopenapi.requests.overseas.inquire.live.InquireOverseasLiveConfirm
import io.github.devngho.kisopenapi.requests.response.balance.overseas.BalanceAccountOverseas
import io.github.devngho.kisopenapi.requests.response.balance.overseas.BalanceAccountStockOverseas
import io.github.devngho.kisopenapi.requests.util.Closeable
import io.github.devngho.kisopenapi.requests.util.Currency
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
class AccountOverseasImpl(val client: KISApiClient, val exchange: OverseasMarket, val currency: Currency) :
    AccountOverseas {
    class AccountStockOverseas(val stock: StockOverseas) : AccountStock, StockOverseas by stock {
        override var count: BigInteger? = null
        var evalAmount: BigDecimal? = null
        override var buyPriceAverage: BigDecimal? = null
    }

    override var assetAmount: BigDecimal? = null
    override var evalAmount: BigDecimal? = null
    override val accountStocks: MutableList<AccountStock> = mutableListOf()

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
        if (res is BalanceAccountOverseas) {
            res.update()
        }
    }

    private fun BalanceAccountOverseas.update() {
        this@AccountOverseasImpl.also {
            it.assetAmount = this.buyAmountTotalByForeignCurrency ?: it.assetAmount
            it.evalAmount =
                ((this.totalProfitLossAmount ?: BigDecimal.fromInt(0)) + (this.buyAmountTotalByForeignCurrency
                    ?: BigDecimal.fromInt(0)))
        }
    }

    private fun List<BalanceAccountStockOverseas>.update() {
        this@AccountOverseasImpl.accountStocks.also { t ->
            t.clear()
            t.addAll(
                this.mapNotNull {
                    if (it.ticker != null) {
                        AccountStockOverseas(
                            StockOverseasImpl(client, it.ticker!!, exchange)
                        ).apply {
                            this.buyPriceAverage = it.buyAveragePrice ?: this.buyPriceAverage
                            this.count = it.count ?: this.count
                            this.evalAmount = it.evalAmount ?: this.evalAmount
                            this.price = it
                            this.name.nameShort = it.productName
                        }
                    } else null
                }
            )
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