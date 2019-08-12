package com.ajw.kotlin.model

import java.util.concurrent.locks.Lock

data class AccountAndLock(val account: Account, val lock: Lock)