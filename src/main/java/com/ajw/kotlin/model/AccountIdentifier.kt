package com.ajw.kotlin.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.common.base.Objects

data class AccountIdentifier @JsonCreator
constructor(@JsonProperty("sortCode") val sortCode: String, @JsonProperty("accountNumber") val accountNumber: String)
