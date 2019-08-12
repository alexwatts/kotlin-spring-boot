package com.ajw.kotlin.service

import com.ajw.kotlin.model.*
import com.ajw.kotlin.service.exception.AccountDetailsInvalidException
import com.ajw.kotlin.service.exception.InsufficientFundsException
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Comparator
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private val INSUFFICIENT_FUNDS_MESSAGE = "Insufficient Funds"
private val ACCOUNT_NOT_FOUND_MESSAGE = "This account was not found. %s/%s"
private val ACCOUNT_IN_INCONSISTENT_STATE_MESSAGE = "Account was in an inconsistent state. Should never be possible."

@Service
class BankingService {

    private val accounts = ConcurrentHashMap<AccountIdentifier, AccountAndLock>()

    fun createAccount(account: Account): Account {
        return getOrCreateAccount(account).account
    }

    @Throws(AccountDetailsInvalidException::class)
    fun getAccount(accountIdentifier: AccountIdentifier): Account {

        return accounts[accountIdentifier]?.account ?:
        messageAccountNotFound(accountIdentifier)
    }

    @Throws(InsufficientFundsException::class, AccountDetailsInvalidException::class)
    fun transfer(transfer: Transfer): Transfer {
        makeTransactions(orderTransactions(validatedTransfer(transfer)))
        return transfer
    }

    private fun getOrCreateAccount(account: Account): AccountAndLock {
        return accounts.computeIfAbsent(
                account.accountIdentifier
               ){ AccountAndLock(account, ReentrantLock()) }
    }

    private fun makeTransactions(transactions: List<Transaction>) {
        transactions.forEach { makeTransaction(getAccountAndLock(it.accountIdentifier), it) }
    }

    private fun makeTransaction(accountAndLock: AccountAndLock, transaction: Transaction) {
        accountAndLock.lock.withLock {
            if (!accounts.replace(
                            accountAndLock.account.accountIdentifier,
                            accountAndLock,
                            AccountAndLock(applyTransactionToAccount(accountAndLock.account, transaction), accountAndLock.lock))
            ) {
               throw IllegalStateException(ACCOUNT_IN_INCONSISTENT_STATE_MESSAGE)
            }
        }
    }

    @Throws(AccountDetailsInvalidException::class)
    private fun validatedTransfer(transfer: Transfer) : Transfer {

        accounts[transfer.sourceAccountIdentifier] ?: messageAccountNotFound(transfer.sourceAccountIdentifier)
        accounts[transfer.destinationAccountIdentifier] ?: messageAccountNotFound(transfer.destinationAccountIdentifier)

        validateSourceAccountHasFunds(
                getAccountAndLock(transfer.sourceAccountIdentifier).account,
                transfer.transferValue
        )

        return transfer
    }

    @Throws(InsufficientFundsException::class)
    private fun validateSourceAccountHasFunds(sourceAccount: Account, transferValue: Money) {
        if (sourceAccount.balance.subtract(transferValue).getMoney().value < BigDecimal.ZERO) {
            throw InsufficientFundsException(INSUFFICIENT_FUNDS_MESSAGE)
        }
    }

    @Throws(AccountDetailsInvalidException::class)
    private fun messageAccountNotFound(accountIdentifier: AccountIdentifier) : Account {
        throw AccountDetailsInvalidException(
                String.format(
                        ACCOUNT_NOT_FOUND_MESSAGE,
                        accountIdentifier.sortCode,
                        accountIdentifier.accountNumber)
        )
    }

    private fun applyTransactionToAccount(
            account: Account,
            transaction: Transaction) : Account {
        return Account(account.accountIdentifier, account.balance.add(transaction.money))
    }

    private fun orderTransactions(transfer: Transfer): List<Transaction> {
        return listOf(
                Transaction(transfer.sourceAccountIdentifier, transfer.transferValue.negate()),
                Transaction(transfer.destinationAccountIdentifier, transfer.transferValue)
               ).sortedWith(sortByAccountNumberComparator())
    }

    private fun getAccountAndLock(accountIdentifier: AccountIdentifier) : AccountAndLock {
        return accounts[accountIdentifier]!!
    }

    private fun sortByAccountNumberComparator() : Comparator<Transaction> {
        return Comparator.comparing { t: Transaction -> t.accountIdentifier.accountNumber }
    }

    fun Money.add(money :Money) : Money {
        return Money(this.currency, this.value.add(money.value).setScale(2, RoundingMode.HALF_EVEN))
    }

    fun Money.subtract(money :Money) : Money {
        return Money(this.currency, this.value.subtract(money.value).setScale(2, RoundingMode.HALF_EVEN))
    }

    fun Money.negate() : Money {
        return Money(this.currency, this.value.negate().setScale(2, RoundingMode.HALF_EVEN))
    }

    fun Money.getMoney() : Money {
        return Money(this.currency, this.value.setScale(2, RoundingMode.HALF_EVEN))
    }

}
