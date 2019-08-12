package com.ajw.kotlin.service.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
class NegativeTransferException(message: String) : Exception(message)