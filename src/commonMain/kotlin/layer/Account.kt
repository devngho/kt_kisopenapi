package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.InquireBalance
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.response.BalanceAccount
import io.github.devngho.kisopenapi.requests.response.BalanceAccountStock
import io.github.devngho.kisopenapi.requests.util.InquireDivisionCode
import kotlin.reflect.KClass

class Account(val client: KisOpenApi) : IAccount {
    override var assetAmount: BigInteger? = null
    override var evalAmount: BigInteger? = null
    override val accountStocks: MutableList<AccountStock> = mutableListOf()

    override suspend fun updateBy(res: KClass<out Response>) {
        when(res.simpleName) {
            BalanceAccount::class.simpleName -> {
                InquireBalance(client).call(InquireBalance.InquireBalanceData(true, InquireDivisionCode.ByStock, true,
                    includeYesterdaySell = false
                )).run {
                    output1?.update()
                    output2?.let {
                        updateBy(it[0])
                    }
                }
            }
        }
    }

    override fun updateBy(res: Response) {
        if (res is BalanceAccount) { res.update() }
    }

    private fun BalanceAccount.update() {
        this@Account.also {
            it.assetAmount = this.netWorthAmount ?: it.assetAmount
            it.evalAmount = evalTotalAmount ?: it.evalAmount
        }
    }

    private fun List<BalanceAccountStock>.update() {
        this@Account.accountStocks.also { t ->
            t.clear()
            t.addAll(
                this.mapNotNull {
                    if (it.productCode != null) {
                        AccountStock(
                            StockDomestic(client, it.productCode!!)
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
}