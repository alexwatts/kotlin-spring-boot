package com.ajw.kotlin.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.common.base.Objects

class AccountIdentifier @JsonCreator
constructor(@JsonProperty("sortCode") val sortCode: String, @JsonProperty("accountNumber") val accountNumber: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as AccountIdentifier?
        return Objects.equal(sortCode, that!!.sortCode) && Objects.equal(accountNumber, that.accountNumber)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(sortCode, accountNumber)
    }

}
