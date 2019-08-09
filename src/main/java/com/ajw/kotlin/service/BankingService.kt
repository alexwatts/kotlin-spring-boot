package com.ajw.kotlin.service

import com.ajw.kotlin.model.*
import com.ajw.kotlin.service.exception.AccountDetailsInvalidException
import com.ajw.kotlin.service.exception.InsufficientFundsException
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.Comparator
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Service
class BankingService {

    private val accounts = ConcurrentHashMap<AccountIdentifier, AccountAndLock>()

    fun createAccount(account: Account): Account {
        return getOrCreateAccount(account).account
    }

    @Throws(AccountDetailsInvalidException::class)
    fun getAccount(accountIdentifier: AccountIdentifier): Account {
        return accounts[accountIdentifier]?.account ?:
        throw AccountDetailsInvalidException(
                String.format("This account was not found. %s/%s",
                        accountIdentifier.sortCode,
                        accountIdentifier.accountNumber)
        )
    }

    @Throws(InsufficientFundsException::class, AccountDetailsInvalidException::class)
    fun transfer(transfer: Transfer): Transfer {

        validateTransfer(transfer)
        makeTransactions(
                orderTransactions(
                        transfer.sourceAccountIdentifier,
                        transfer.destinationAccountIdentifier,
                        transfer.transferValue
                )
        )
        return transfer
    }

    private fun getOrCreateAccount(account: Account): AccountAndLock {
        return accounts.computeIfAbsent(
                account.accountIdentifier
               ){ ac -> AccountAndLock(Account(ac, account.balance), ReentrantLock()) }
    }

    private fun makeTransactions(transactions: List<Transaction>) {
        transactions.forEach { this.makeTransaction( getAccountAndLock(it.accountIdentifier), it) }
    }

    private fun makeTransaction(accountAndLock: AccountAndLock, transaction: Transaction) {
        accountAndLock.lock.withLock {
            if (!accounts.replace(
                            accountAndLock.account.accountIdentifier,
                            accountAndLock,
                            addTransactionToAccount(accountAndLock, transaction))
            ) {
               throw IllegalStateException("Account was in an inconsistent state. Should never be possible.")
            }
        }
    }

    @Throws(AccountDetailsInvalidException::class)
    private fun validateTransfer(transfer: Transfer) {

        accounts[transfer.sourceAccountIdentifier] ?: messageAccountNotFound(transfer.sourceAccountIdentifier)
        accounts[transfer.destinationAccountIdentifier] ?: messageAccountNotFound(transfer.destinationAccountIdentifier)

        validateSourceAccountHasFunds(
                getAccountAndLock(transfer.sourceAccountIdentifier).account,
                transfer.transferValue
        )
    }

    @Throws(InsufficientFundsException::class)
    private fun validateSourceAccountHasFunds(sourceAccount: Account, transferValue: Money) {
        if (sourceAccount.balance.subtract(transferValue).getValue() < BigDecimal.ZERO) {
            throw InsufficientFundsException("Insufficient Funds")
        }
    }

    @Throws(AccountDetailsInvalidException::class)
    private fun messageAccountNotFound(accountIdentifier: AccountIdentifier) {
        throw AccountDetailsInvalidException(
                String.format(
                        "This account was not found. %s/%s",
                        accountIdentifier.sortCode,
                        accountIdentifier.accountNumber)
        )
    }

    private fun addTransactionToAccount(
            accountAndLock: AccountAndLock,
            transaction: Transaction) : AccountAndLock {

                return AccountAndLock(
                        Account(
                                accountAndLock.account.accountIdentifier,
                                accountAndLock.account.balance.add(transaction.money)
                        ),
                        accountAndLock.lock
                )

    }

    private fun orderTransactions(
            sourceAccountIdentifier: AccountIdentifier,
            destinationAccountIdentifier: AccountIdentifier,
            transactionAmount: Money): List<Transaction> {

        return listOf(
                Transaction(sourceAccountIdentifier, transactionAmount.negate()),
                Transaction(destinationAccountIdentifier, transactionAmount)
               ).sortedWith(sortByAccountNumberComparator())
    }

    private fun getAccountAndLock(accountIdentifier: AccountIdentifier) : AccountAndLock {
        return accounts[accountIdentifier]!!
    }

    private fun sortByAccountNumberComparator() : Comparator<Transaction> {
        return Comparator.comparing { t: Transaction -> t.accountIdentifier.accountNumber }
    }

}
