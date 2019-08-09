package com.ajw.kotlin.controller

import com.ajw.kotlin.model.Account
import com.ajw.kotlin.model.AccountIdentifier
import com.ajw.kotlin.service.BankingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = ["/accounts"])
class AccountController {

    @Autowired
    lateinit var bankingService: BankingService

    @ResponseBody
    @RequestMapping(consumes = ["application/json"], produces = ["application/json"], method = [RequestMethod.PUT])
    fun createAccount(@RequestBody account: Account): ResponseEntity<Account> {
        return ResponseEntity(bankingService.createAccount(account), HttpStatus.CREATED)
    }

    @ResponseBody
    @RequestMapping(value = ["{sortCode}/{accountNumber}"], produces = ["application/json"], method = [RequestMethod.GET])
    fun getAccount(@PathVariable("sortCode") sortCode: String, @PathVariable("accountNumber") accountNumber: String): ResponseEntity<Account> {
        val accountIdentifier = AccountIdentifier(sortCode, accountNumber)
        return ResponseEntity(bankingService.getAccount(accountIdentifier), HttpStatus.OK)
    }

}