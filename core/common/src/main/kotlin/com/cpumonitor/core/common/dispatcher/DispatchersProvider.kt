package com.cpumonitor.core.common.dispatcher

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Provides coroutine dispatchers for dependency injection and testability.
 */
interface DispatchersProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}
