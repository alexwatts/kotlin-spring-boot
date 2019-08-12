package com.ajw.kotlin.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

data class Money @JsonCreator
constructor(
        @JsonProperty("currency") val currency: Currency,
        @JsonProperty("value") private val value: BigDecimal) {

    operator fun plus(money: Money): Money =
            Money(this.currency, this.value.add(money.value).setScale(scale(), rounding()))

    operator fun minus(money: Money): Money =
            Money(this.currency, this.value.subtract(money.value).setScale(scale(), rounding()))

    fun negate() : Money {
        return Money(this.currency, this.value.negate().setScale(scale(), rounding()))
    }

    fun getValue() : BigDecimal {
        return this.value.setScale(scale(), rounding())
    }

    private fun scale() : Int {
        return 2
    }

    private fun rounding() : RoundingMode {
        return RoundingMode.HALF_EVEN
    }

}
