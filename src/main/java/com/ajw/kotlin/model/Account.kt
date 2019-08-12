package com.ajw.kotlin.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.common.base.Objects

data class Account @JsonCreator
constructor(
        @JsonProperty("accountIdentifier") val accountIdentifier: AccountIdentifier,
        @JsonProperty("balance") val balance: Money)
