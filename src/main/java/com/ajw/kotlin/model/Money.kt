package com.ajw.kotlin.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.common.base.Objects

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

class Money @JsonCreator
constructor(
        @JsonProperty("currency") val currency: Currency,
        @JsonProperty("value") private val value: BigDecimal) {

    fun add(money: Money): Money {
        return Money(money.currency, getValue().add(money.getValue()))
    }

    fun subtract(money: Money): Money {
        return Money(money.currency, getValue().subtract(money.getValue()))
    }

    fun negate(): Money {
        return Money(this.currency, getValue().negate())
    }

    fun getValue(): BigDecimal {
        return this.value.setScale(2, RoundingMode.HALF_EVEN)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val money = other as Money?
        return Objects.equal(currency, money!!.currency) && Objects.equal(getValue(), money.getValue())
    }

    override fun hashCode(): Int {
        return Objects.hashCode(currency, getValue())
    }

    override fun toString(): String {
        return "Money{" +
                "currency=" + currency +
                ", value=" + value +
                '}'.toString()
    }

}
