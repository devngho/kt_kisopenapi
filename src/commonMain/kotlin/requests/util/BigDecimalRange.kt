package io.github.devngho.kisopenapi.requests.util

import com.ionspin.kotlin.bignum.decimal.BigDecimal

class BigDecimalRange(override val start: BigDecimal, override val endInclusive: BigDecimal) : ClosedRange<BigDecimal> {
    override fun contains(value: BigDecimal): Boolean = value in start..endInclusive
    constructor(start: Double, endInclusive: Double) : this(BigDecimal.fromDouble(start), BigDecimal.fromDouble(endInclusive))
}