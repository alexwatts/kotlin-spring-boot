package com.ajw.kotlin.controller

import com.ajw.kotlin.app.TestApplication
import com.ajw.kotlin.model.AccountIdentifier
import com.ajw.kotlin.model.Money
import com.ajw.kotlin.model.Transfer
import com.ajw.kotlin.service.BankingService
import com.ajw.kotlin.service.exception.AccountDetailsInvalidException
import com.ajw.kotlin.service.exception.InsufficientFundsException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.test.context.junit4.SpringRunner

import java.math.BigDecimal
import java.util.Currency
import java.util.Locale

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.mockito.Mockito.`when`

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [TestApplication::class])
class TransferControllerTest {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @MockBean
    lateinit var bankingService: BankingService

    var headers: HttpHeaders = HttpHeaders()

    @Before
    fun setUp() {
        headers.contentType = MediaType.APPLICATION_JSON
        headers.accept = listOf(MediaType.APPLICATION_JSON)
    }

    @Test
    @Throws(Exception::class)
    fun shouldBeAbleToMakeTransfer() {

        val sourceAccount = AccountIdentifier("67-23-65", "4773267")
        val destinationAccount = AccountIdentifier("23-54-77", "234234")

        val aTransfer = Transfer(sourceAccount, destinationAccount, Money(Currency.getInstance(Locale.UK), BigDecimal("17.64")))

        `when`(bankingService.transfer(aTransfer)).thenReturn(aTransfer)

        val createdTransfer = testRestTemplate.exchange("/transfers", HttpMethod.PUT, HttpEntity(aTransfer, headers), object : ParameterizedTypeReference<Transfer>() {

        })

        assertThat(createdTransfer.statusCode, equalTo(HttpStatus.CREATED))
        assertThat(createdTransfer.body, equalTo(aTransfer))
    }

    @Test
    @Throws(Exception::class)
    fun shouldNotMakeTransferWhenInsufficientFunds() {

        val sourceAccount = AccountIdentifier("67-23-65", "4773267")
        val destinationAccount = AccountIdentifier("23-54-77", "234234")

        val aTransfer = Transfer(sourceAccount, destinationAccount, Money(Currency.getInstance(Locale.UK), BigDecimal("17.64")))

        `when`(bankingService.transfer(aTransfer)).thenThrow(InsufficientFundsException("Insufficient funds"))

        val exchange = testRestTemplate.exchange("/transfers", HttpMethod.PUT, HttpEntity(aTransfer, headers), InsufficientFundsException::class.java)

        assertThat(exchange.statusCode, equalTo(HttpStatus.UNPROCESSABLE_ENTITY))
        assertThat(exchange.body!!.message, equalTo("Insufficient funds"))
    }

    @Test
    @Throws(Exception::class)
    fun shouldNotMakeTransferWhenAccountDetailsInvalid() {

        val sourceAccount = AccountIdentifier("67-23-65", "4773267")
        val destinationAccount = AccountIdentifier("23-54-77", "234234")

        val aTransfer = Transfer(sourceAccount, destinationAccount, Money(Currency.getInstance(Locale.UK), BigDecimal("17.64")))

        `when`(bankingService.transfer(aTransfer)).thenThrow(AccountDetailsInvalidException("This account was not found. 67-23-65/4773267"))

        val exchange = testRestTemplate.exchange("/transfers", HttpMethod.PUT, HttpEntity(aTransfer, headers), AccountDetailsInvalidException::class.java)

        assertThat(exchange.statusCode, equalTo(HttpStatus.UNPROCESSABLE_ENTITY))
        assertThat(exchange.body!!.message, equalTo("This account was not found. 67-23-65/4773267"))
    }

}
