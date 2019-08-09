package com.ajw.kotlin.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.common.base.Objects

class Account @JsonCreator
constructor(
        @JsonProperty("accountIdentifier") val accountIdentifier: AccountIdentifier,
        @JsonProperty("balance") val balance: Money) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val account = other as Account?
        return Objects.equal(accountIdentifier, account!!.accountIdentifier) && Objects.equal(balance, account.balance)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(accountIdentifier, balance)
    }

}
