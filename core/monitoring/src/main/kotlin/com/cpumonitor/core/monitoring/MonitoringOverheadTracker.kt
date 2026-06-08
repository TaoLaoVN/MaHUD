package com.cpumonitor.core.monitoring

import com.cpumonitor.domain.performance.PerformanceBudget
import java.util.ArrayDeque

/**
 * Tracks monitoring poll overhead to validate the 2% CPU budget (Section 9).
 */
object MonitoringOverheadTracker {

    private const val MAX_SAMPLES = 60

    private val samples = ArrayDeque<Sample>()
    private var totalPollCount = 0L

    data class Snapshot(
        val overheadPercent: Float,
        val averagePollDurationMs: Float,
        val pollCount: Long,
        val withinBudget: Boolean,
    )

    private data class Sample(
        val durationMs: Long,
        val intervalMs: Long,
    )

    @Synchronized
    fun recordPoll(durationMs: Long, intervalMs: Long) {
        if (intervalMs <= 0L) return
        samples.addLast(Sample(durationMs.coerceAtLeast(0L), intervalMs))
        while (samples.size > MAX_SAMPLES) {
            samples.removeFirst()
        }
        totalPollCount++
    }

    @Synchronized
    fun snapshot(): Snapshot {
        if (samples.isEmpty()) {
            return Snapshot(
                overheadPercent = 0f,
                averagePollDurationMs = 0f,
                pollCount = totalPollCount,
                withinBudget = true,
            )
        }

        val overheadPercent = samples
            .map { (it.durationMs.toFloat() / it.intervalMs.toFloat()) * 100f }
            .average()
            .toFloat()

        val averageDuration = samples.map { it.durationMs.toFloat() }.average().toFloat()

        return Snapshot(
            overheadPercent = overheadPercent,
            averagePollDurationMs = averageDuration,
            pollCount = totalPollCount,
            withinBudget = PerformanceBudget.isCpuOverheadWithinBudget(overheadPercent),
        )
    }

    @Synchronized
    fun reset() {
        samples.clear()
        totalPollCount = 0L
    }
}
