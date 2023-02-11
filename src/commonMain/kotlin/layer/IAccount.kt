package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.integer.BigInteger

interface IAccount : Updatable{
    val assetAmount: BigInteger?
    val evalAmount: BigInteger?
    val accountStocks: MutableList<AccountStock>
}