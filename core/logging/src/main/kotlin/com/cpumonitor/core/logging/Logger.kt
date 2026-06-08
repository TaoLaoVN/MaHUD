package com.cpumonitor.core.logging

import timber.log.Timber

/**
 * Application-wide logging facade backed by Timber.
 */
object Logger {
    fun init(isDebug: Boolean) {
        if (isDebug) {
            Timber.plant(Timber.DebugTree())
        }
    }

    fun d(message: String) = Timber.d(message)
    fun i(message: String) = Timber.i(message)
    fun w(message: String) = Timber.w(message)
    fun e(message: String, throwable: Throwable? = null) = Timber.e(throwable, message)
}
