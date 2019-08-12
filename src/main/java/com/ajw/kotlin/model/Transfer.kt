package com.ajw.kotlin.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.common.base.Objects

data class Transfer
@JsonCreator
constructor(
        @JsonProperty("sourceAccountIdentifier") val sourceAccountIdentifier: AccountIdentifier,
        @JsonProperty("destinationAccountIdentifier") val destinationAccountIdentifier: AccountIdentifier,
        @JsonProperty("transferValue") val transferValue: Money)