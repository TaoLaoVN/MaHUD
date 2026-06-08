package com.cpumonitor.domain.performance

/**
 * Application performance targets from Section 9 of the master plan.
 */
object PerformanceBudget {
    const val DASHBOARD_REFRESH_INTERVAL_MS = 1_000L
    const val MIN_BACKGROUND_REFRESH_SECONDS = 5
    const val MAX_BACKGROUND_REFRESH_SECONDS = 60
    const val MAX_CPU_OVERHEAD_PERCENT = 2f
    const val MAX_MEMORY_FOOTPRINT_BYTES = 150L * 1024L * 1024L

    fun isCpuOverheadWithinBudget(overheadPercent: Float): Boolean =
        overheadPercent <= MAX_CPU_OVERHEAD_PERCENT

    fun isMemoryWithinBudget(usedBytes: Long): Boolean =
        usedBytes <= MAX_MEMORY_FOOTPRINT_BYTES
}
