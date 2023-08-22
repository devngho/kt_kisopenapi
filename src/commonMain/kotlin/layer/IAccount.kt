package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger

interface IAccount : Updatable{
    val accountStocks: MutableList<AccountStock>
}

interface IAccountDomestic : IAccount{
    val assetAmount: BigInteger?
    val evalAmount: BigInteger?
    override val accountStocks: MutableList<AccountStock>
}

interface IAccountOverseas : IAccount{
    val assetAmount: BigDecimal?
    val evalAmount: BigDecimal?
    override val accountStocks: MutableList<AccountStock>
}