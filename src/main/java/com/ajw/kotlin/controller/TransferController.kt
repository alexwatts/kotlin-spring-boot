package com.ajw.kotlin.controller

import com.ajw.kotlin.model.Transfer
import com.ajw.kotlin.service.BankingService
import com.ajw.kotlin.service.exception.AccountDetailsInvalidException
import com.ajw.kotlin.service.exception.InsufficientFundsException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = ["/transfers"])
class TransferController {

    @Autowired
    lateinit var bankingService: BankingService

    @ResponseBody
    @RequestMapping(consumes = ["application/json"], produces = ["application/json"], method = [RequestMethod.PUT])
    @Throws(InsufficientFundsException::class, AccountDetailsInvalidException::class)
    fun createTransfer(@RequestBody transfer: Transfer): ResponseEntity<Transfer> {
        return ResponseEntity(bankingService.transfer(transfer), HttpStatus.CREATED)
    }

}