package com.ajw.kotlin.model

import com.google.common.base.Objects

import java.util.concurrent.locks.Lock

class AccountAndLock(val account: Account, val lock: Lock) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as AccountAndLock?
        return Objects.equal(account, that!!.account) && Objects.equal(lock, that.lock)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(account, lock)
    }
}
