package com.ajw.kotlin.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.common.base.Objects

class Transfer
@JsonCreator
constructor(
        @JsonProperty("sourceAccountIdentifier") val sourceAccountIdentifier: AccountIdentifier,
        @JsonProperty("destinationAccountIdentifier") val destinationAccountIdentifier: AccountIdentifier,
        @JsonProperty("transferValue") val transferValue: Money) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val transfer = other as Transfer?
        return Objects.equal(sourceAccountIdentifier, transfer!!.sourceAccountIdentifier) &&
                Objects.equal(destinationAccountIdentifier, transfer.destinationAccountIdentifier) &&
                Objects.equal(transferValue, transfer.transferValue)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(sourceAccountIdentifier, destinationAccountIdentifier, transferValue)
    }

}
