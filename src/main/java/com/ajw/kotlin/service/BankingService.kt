package com.ajw.kotlin.service

import com.ajw.kotlin.model.*
import com.ajw.kotlin.service.exception.AccountDetailsInvalidException
import com.ajw.kotlin.service.exception.InsufficientFundsException
import com.ajw.kotlin.service.exception.NegativeTransferException
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.Comparator
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

const val INSUFFICIENT_FUNDS_MESSAGE = "Insufficient Funds."
const val ACCOUNT_NOT_FOUND_MESSAGE = "This account was not found. %s/%s."
const val ACCOUNT_IN_INCONSISTENT_STATE_MESSAGE = "Account was in an inconsistent state. Should never be possible."
const val NEGATIVE_TRANSFER_MESSAGE = "Negative Transfers cannot be made."

@Service
class BankingService {

    private val accounts = ConcurrentHashMap<AccountIdentifier, AccountAndLock>()

    fun createAccount(account: Account): Account {
        return getOrCreateAccount(account).account
    }

    @Throws(AccountDetailsInvalidException::class)
    fun getAccount(accountIdentifier: AccountIdentifier): Account {
        return accounts[accountIdentifier]?.account ?: messageAccountNotFound(accountIdentifier)
    }

    @Throws(
        InsufficientFundsException::class,
        AccountDetailsInvalidException::class,
        NegativeTransferException::class
    )
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
                            AccountAndLock(
                                    applyTransactionToAccount(accountAndLock.account, transaction),
                                    accountAndLock.lock
                            ))
            ) {
               throw IllegalStateException(ACCOUNT_IN_INCONSISTENT_STATE_MESSAGE)
            }
        }
    }

    @Throws(AccountDetailsInvalidException::class)
    private fun validatedTransfer(transfer: Transfer) : Transfer {

        validateAccounts(transfer.sourceAccountIdentifier, transfer.destinationAccountIdentifier)

        validateTransferAmount(transfer.transferValue)

        validateSourceAccountHasFunds(
                getAccountAndLock(transfer.sourceAccountIdentifier).account,
                transfer.transferValue
        )

        return transfer
    }

    @Throws(AccountDetailsInvalidException::class)
    private fun validateAccounts(vararg accountIdentifiers: AccountIdentifier) {
        accountIdentifiers.forEach { accounts[it] ?: messageAccountNotFound(it) }
    }

    @Throws(InsufficientFundsException::class)
    private fun validateSourceAccountHasFunds(sourceAccount: Account, transferValue: Money) {
        if ((sourceAccount.balance - transferValue).getValue() < BigDecimal.ZERO) {
            throw InsufficientFundsException(INSUFFICIENT_FUNDS_MESSAGE)
        }
    }

    @Throws(NegativeTransferException::class)
    private fun validateTransferAmount(money: Money) {
        if (money.getValue() < BigDecimal.ZERO) {
            throw NegativeTransferException((NEGATIVE_TRANSFER_MESSAGE))
        }
    }

    @Throws(AccountDetailsInvalidException::class)
    private fun messageAccountNotFound(accountIdentifier: AccountIdentifier) : Account {
        throw AccountDetailsInvalidException(
                String.format(
                        ACCOUNT_NOT_FOUND_MESSAGE,
                        accountIdentifier.sortCode,
                        accountIdentifier.accountNumber
                )
        )
    }

    private fun applyTransactionToAccount(
            account: Account,
            transaction: Transaction) : Account {
        return Account(account.accountIdentifier, account.balance + transaction.money)
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

}
