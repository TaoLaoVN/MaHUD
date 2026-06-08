package com.cpumonitor.core.monitoring

import com.cpumonitor.domain.performance.PerformanceBudget

/**
 * Lightweight JVM memory snapshot for performance diagnostics.
 */
object AppMemorySnapshot {

    data class Reading(
        val usedBytes: Long,
        val maxBytes: Long,
        val withinBudget: Boolean,
    )

    fun current(): Reading {
        val runtime = Runtime.getRuntime()
        val usedBytes = runtime.totalMemory() - runtime.freeMemory()
        val maxBytes = runtime.maxMemory()
        return Reading(
            usedBytes = usedBytes,
            maxBytes = maxBytes,
            withinBudget = PerformanceBudget.isMemoryWithinBudget(usedBytes),
        )
    }

    fun formatMegabytes(bytes: Long): String =
        "%.1f MB".format(bytes.toDouble() / (1024.0 * 1024.0))
}
