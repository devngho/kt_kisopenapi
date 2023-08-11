package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger

class AccountStock(val stock: IStockDomestic) : IStockDomestic by stock{
    var count: BigInteger? = null
    var evalAmount: BigInteger? = null
    var buyPriceAverage: BigDecimal? = null
}