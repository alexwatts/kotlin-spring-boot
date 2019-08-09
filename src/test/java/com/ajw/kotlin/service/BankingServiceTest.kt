package com.ajw.kotlin.service

import com.ajw.kotlin.model.Account
import com.ajw.kotlin.model.AccountIdentifier
import com.ajw.kotlin.model.Money
import com.ajw.kotlin.model.Transfer
import com.ajw.kotlin.service.exception.AccountDetailsInvalidException
import com.ajw.kotlin.service.exception.InsufficientFundsException
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.math.BigDecimal
import java.util.Currency
import java.util.Locale
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo

class BankingServiceTest {

    @get:Rule
    var exception: ExpectedException = ExpectedException.none()

    private val subject = BankingService()

    @Test
    fun shouldBeAbleToCreateAccount() {
        val testAccount = Account(AccountIdentifier("create", "account"), givenMoney("00.20"))
        assertThat(subject.createAccount(testAccount), equalTo(testAccount))
    }

    @Test
    fun shouldBeAbleToGetAccount() {
        val account = givenAccount("1", givenMoney("00.10"))
        assertThat(subject.getAccount(account.accountIdentifier), equalTo(account))
    }

    @Test
    @Throws(Exception::class)
    fun shouldBeAbleToMakeTransfer() {
        val sourceAccount = givenAccount("1", givenMoney("100.00"))
        val destinationAccount = givenAccount("2", givenMoney("00.00"))

        subject.transfer(Transfer(sourceAccount.accountIdentifier, destinationAccount.accountIdentifier, givenMoney("00.20")))
        val retrievedSourceAccount = subject.getAccount(sourceAccount.accountIdentifier)
        val retrievedDestinationAccount = subject.getAccount(destinationAccount.accountIdentifier)

        assertThat(retrievedSourceAccount.balance, equalTo(givenMoney("99.80")))
        assertThat(retrievedDestinationAccount.balance, equalTo(givenMoney("00.20")))
    }

    @Test
    @Throws(Exception::class)
    fun shouldBeAbleToMakeTransferWithInvalidScales() {
        val sourceAccount = givenAccount("1", givenMoney("100.00234342123123124"))
        val destinationAccount = givenAccount("2", givenMoney("00.0000000001"))

        subject.transfer(Transfer(sourceAccount.accountIdentifier, destinationAccount.accountIdentifier, givenMoney("00.20042340234123")))
        val retrievedSourceAccount = subject.getAccount(sourceAccount.accountIdentifier)
        val retrievedDestinationAccount = subject.getAccount(destinationAccount.accountIdentifier)

        assertThat(retrievedSourceAccount.balance, equalTo(givenMoney("99.80")))
        assertThat(retrievedDestinationAccount.balance, equalTo(givenMoney("00.20")))
    }

    @Test
    @Throws(Exception::class)
    fun shouldBeAbleToMakeTransferWhenFundsExactlySameAsTransferAmount() {
        val sourceAccount = givenAccount("1", givenMoney("100.00"))
        val destinationAccount = givenAccount("2", givenMoney("00.00"))

        subject.transfer(Transfer(sourceAccount.accountIdentifier, destinationAccount.accountIdentifier, givenMoney("100.00")))

        val retrievedSourceAccount = subject.getAccount(sourceAccount.accountIdentifier)
        val retrievedDestinationAccount = subject.getAccount(destinationAccount.accountIdentifier)

        assertThat(retrievedSourceAccount.balance, equalTo(givenMoney("00.00")))
        assertThat(retrievedDestinationAccount.balance, equalTo(givenMoney("100.00")))
    }

    @Test
    @Throws(Exception::class)
    fun shouldNotBeAbleToMakeTransferWhenInsufficientFunds() {
        exception.expect(InsufficientFundsException::class.java)
        exception.expectMessage("Insufficient Funds")

        val sourceAccount = givenAccount("1", givenMoney("00.00"))
        val destinationAccount = givenAccount("2", givenMoney("00.00"))

        subject.transfer(Transfer(sourceAccount.accountIdentifier, destinationAccount.accountIdentifier, givenMoney("00.20")))
    }

    @Test
    @Throws(Exception::class)
    fun shouldNotBeAbleToMakeTransferWhenSourceAccountDoesNotExist() {
        exception.expect(AccountDetailsInvalidException::class.java)
        exception.expectMessage("This account was not found. nonsense/account")

        val destinationAccount = givenAccount("2", givenMoney("00.00"))

        subject.transfer(Transfer(AccountIdentifier("nonsense", "account"), destinationAccount.accountIdentifier, givenMoney("00.20")))
    }

    @Test
    @Throws(Exception::class)
    fun shouldNotBeAbleToMakeTransferWhenDestinationAccountDoesNotExist() {
        exception.expect(AccountDetailsInvalidException::class.java)
        exception.expectMessage("This account was not found. nonsense/account")

        val sourceAccount = givenAccount("1", givenMoney("00.00"))

        subject.transfer(Transfer(sourceAccount.accountIdentifier, AccountIdentifier("nonsense", "account"), givenMoney("00.20")))
    }

    private fun givenMoney(money: String): Money {
        return Money(Currency.getInstance(Locale.UK), BigDecimal(money))
    }

    private fun givenAccount(accountId: String, money: Money): Account {
        val account = Account(AccountIdentifier("test", accountId), money)
        subject.createAccount(account)
        return account
    }

}
