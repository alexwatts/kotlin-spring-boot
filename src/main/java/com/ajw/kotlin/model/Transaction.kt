package com.ajw.kotlin.model

import com.google.common.base.Objects

class Transaction(val accountIdentifier: AccountIdentifier, val money: Money) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Transaction?
        return Objects.equal(accountIdentifier, that!!.accountIdentifier) && Objects.equal(money, that.money)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(accountIdentifier, money)
    }

}

