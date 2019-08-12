package com.ajw.kotlin.controller

import com.ajw.kotlin.app.TestApplication
import com.ajw.kotlin.model.Account
import com.ajw.kotlin.model.AccountIdentifier
import com.ajw.kotlin.model.Money
import com.ajw.kotlin.service.BankingService
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
import java.util.Collections
import java.util.Currency
import java.util.Locale

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.mockito.Mockito.`when`

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [TestApplication::class])
class AccountControllerTest {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @MockBean
    lateinit var bankingService: BankingService

    private var headers: HttpHeaders = HttpHeaders()

    @Before
    fun setUp() {
        headers.contentType = MediaType.APPLICATION_JSON
        headers.accept = listOf(MediaType.APPLICATION_JSON)
    }

    @Test
    fun shouldBeAbleToCreateAnAccount() {
        val anAccount = Account(AccountIdentifier("67-23-65", "4773267"), givenMoney("0.00"))

        `when`(bankingService.createAccount(anAccount)).thenReturn(anAccount)

        val createdAccount = testRestTemplate.exchange("/accounts", HttpMethod.PUT, HttpEntity(anAccount, headers), object : ParameterizedTypeReference<Account>() {

        })

        assertThat(createdAccount.statusCode, equalTo(HttpStatus.CREATED))
        assertThat(createdAccount.body, equalTo(anAccount))
    }

    @Test
    fun shouldBeAbleToGetAccount() {

        val accountIdentifier = AccountIdentifier("67-23-65", "4773267")
        val anAccount = Account(accountIdentifier, givenMoney("0.00"))

        `when`(bankingService.getAccount(accountIdentifier)).thenReturn(anAccount)

        val retrievedAccount = testRestTemplate.exchange(String.format("/accounts/%s/%s", accountIdentifier.sortCode, accountIdentifier.accountNumber), HttpMethod.GET, null, object : ParameterizedTypeReference<Account>() {

        })

        assertThat(retrievedAccount.statusCode, equalTo(HttpStatus.OK))
        assertThat(retrievedAccount.body, equalTo(anAccount))
    }

    private fun givenMoney(money: String): Money {
        return Money(Currency.getInstance(Locale.UK), BigDecimal(money))
    }
}
