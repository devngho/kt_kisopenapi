package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger

interface AccountStock : StockBase {
    var count: BigInteger?
    var buyPriceAverage: BigDecimal?
}


