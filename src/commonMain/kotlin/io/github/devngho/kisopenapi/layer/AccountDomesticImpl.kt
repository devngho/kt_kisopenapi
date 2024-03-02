package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.domestic.inquire.InquireBalance
import io.github.devngho.kisopenapi.requests.domestic.inquire.live.InquireLiveConfirm
import io.github.devngho.kisopenapi.requests.response.balance.domestic.BalanceAccount
import io.github.devngho.kisopenapi.requests.response.balance.domestic.BalanceAccountStock
import io.github.devngho.kisopenapi.requests.util.Closeable
import io.github.devngho.kisopenapi.requests.util.InquireDivisionCode
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

class AccountDomesticImpl(val client: KISApiClient) : AccountDomestic {
    class AccountStockDomestic(val stock: StockDomestic) : AccountStock, StockDomestic by stock {
        override var count: BigInteger? = null
        var evalAmount: BigInteger? = null
        override var buyPriceAverage: BigDecimal? = null
    }


    override var assetAmount: BigInteger? = null
    override var evalAmount: BigInteger? = null
    override val accountStocks: MutableList<AccountStock> = mutableListOf()

    override suspend fun update(res: KClass<out Response>) {
        when (res) {
            BalanceAccount::class -> {
                InquireBalance(client).call(
                    InquireBalance.InquireBalanceData(
                        true, InquireDivisionCode.ByStock, true,
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
        if (res is BalanceAccount) {
            res.update()
        }
    }

    private fun BalanceAccount.update() {
        this@AccountDomesticImpl.also {
            it.assetAmount = this.netWorthAmount ?: it.assetAmount
            it.evalAmount = evalTotalAmount ?: it.evalAmount
        }
    }

    private fun List<BalanceAccountStock>.update() {
        this@AccountDomesticImpl.accountStocks.also { t ->
            t.clear()
            t.addAll(
                this.mapNotNull {
                    if (it.ticker != null) {
                        AccountStockDomestic(
                            StockDomesticImpl(client, it.ticker!!)
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