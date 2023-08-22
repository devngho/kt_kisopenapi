package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.InquireBalance
import io.github.devngho.kisopenapi.requests.InquireOverseasBalance
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.response.BalanceAccount
import io.github.devngho.kisopenapi.requests.response.BalanceAccountOverseas
import io.github.devngho.kisopenapi.requests.response.BalanceAccountStock
import io.github.devngho.kisopenapi.requests.response.BalanceAccountStockOverseas
import io.github.devngho.kisopenapi.requests.util.Currency
import io.github.devngho.kisopenapi.requests.util.InquireDivisionCode
import io.github.devngho.kisopenapi.requests.util.OverseasMarket
import kotlin.reflect.KClass

class AccountDomestic(val client: KisOpenApi) : IAccountDomestic {
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
        this@AccountDomestic.also {
            it.assetAmount = this.netWorthAmount ?: it.assetAmount
            it.evalAmount = evalTotalAmount ?: it.evalAmount
        }
    }

    private fun List<BalanceAccountStock>.update() {
        this@AccountDomestic.accountStocks.also { t ->
            t.clear()
            t.addAll(
                this.mapNotNull {
                    if (it.productCode != null) {
                        AccountStockDomestic(
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

class AccountOverseas(val client: KisOpenApi, val exchange: OverseasMarket, val currency: Currency) : IAccountOverseas {
    override var assetAmount: BigDecimal? = null
    override var evalAmount: BigDecimal? = null
    override val accountStocks: MutableList<AccountStock> = mutableListOf()

    override suspend fun updateBy(res: KClass<out Response>) {
        when(res.simpleName) {
            BalanceAccountOverseas::class.simpleName -> {
                InquireOverseasBalance(client).call(InquireOverseasBalance.InquireBalanceData(exchange, currency)).run {
                    output1?.update()
                    output2?.let { updateBy(it) }
                }
            }
        }
    }

    override fun updateBy(res: Response) {
        if (res is BalanceAccountOverseas) { res.update() }
    }

    private fun BalanceAccountOverseas.update() {
        this@AccountOverseas.also {
            it.assetAmount = this.buyAmountTotalByForeignCurrency ?: it.assetAmount
            it.evalAmount =
                ((this.totalProfitLossAmount ?: BigDecimal.fromInt(0)) + (this.buyAmountTotalByForeignCurrency ?: BigDecimal.fromInt(0)))
        }
    }

    private fun List<BalanceAccountStockOverseas>.update() {
        this@AccountOverseas.accountStocks.also { t ->
            t.clear()
            t.addAll(
                this.mapNotNull {
                    if (it.productCode != null) {
                        AccountStockOverseas(
                            StockOverseas(client, it.productCode!!, exchange)
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