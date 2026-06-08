package com.cpumonitor.data.util

import com.cpumonitor.core.monitoring.MonitoringOverheadTracker
import com.cpumonitor.domain.repository.MonitoringConstants
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.math.max
import kotlin.system.measureTimeMillis

/**
 * Creates a lightweight polling [Flow] tuned for the monitoring overhead budget.
 *
 * @param intervalMs Desired interval between samples. Clamped to [MonitoringConstants.MIN_REFRESH_INTERVAL_MS].
 * @param reader Suspend function that performs the actual system read on each tick.
 */
internal fun <T> monitoringPollFlow(
    intervalMs: Long,
    reader: suspend () -> T,
): Flow<T> {
    val safeInterval = max(intervalMs, MonitoringConstants.MIN_REFRESH_INTERVAL_MS)
    return flow {
        while (currentCoroutineContext().isActive) {
            val value = measureTimedPoll(safeInterval, reader)
            emit(value)
            delay(safeInterval)
        }
    }
}

/**
 * Polls a delta-based metric that requires a warm-up sample before the first emission.
 */
internal fun <S, T> monitoringDeltaFlow(
    intervalMs: Long,
    sample: suspend () -> S,
    transform: (previous: S, current: S) -> T,
): Flow<T> {
    val safeInterval = max(intervalMs, MonitoringConstants.MIN_REFRESH_INTERVAL_MS)
    return flow {
        var previous = sample()
        delay(safeInterval)
        while (currentCoroutineContext().isActive) {
            val value = measureTimedPoll(safeInterval) {
                val current = sample()
                transform(previous, current).also { previous = current }
            }
            emit(value)
            delay(safeInterval)
        }
    }
}

private suspend fun <T> measureTimedPoll(
    intervalMs: Long,
    block: suspend () -> T,
): T {
    var result: T
    val durationMs = measureTimeMillis {
        result = block()
    }
    MonitoringOverheadTracker.recordPoll(durationMs, intervalMs)
    return result
}

/**
 * Suppresses consecutive identical emissions to reduce downstream work.
 */
internal fun <T> Flow<T>.distinctUntilChangedBy(selector: (T) -> Any?): Flow<T> =
    distinctUntilChanged { old, new -> selector(old) == selector(new) }
