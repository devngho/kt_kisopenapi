package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger

interface AccountStock: IStockBase{
    var count: BigInteger?
    var buyPriceAverage: BigDecimal?
}

class AccountStockDomestic(val stock: IStockDomestic) : AccountStock, IStockDomestic by stock{
    override var count: BigInteger? = null
    var evalAmount: BigInteger? = null
    override var buyPriceAverage: BigDecimal? = null
}

class AccountStockOverseas(val stock: IStockOverseas) : AccountStock, IStockOverseas by stock{
    override var count: BigInteger? = null
    var evalAmount: BigDecimal? = null
    override var buyPriceAverage: BigDecimal? = null
}