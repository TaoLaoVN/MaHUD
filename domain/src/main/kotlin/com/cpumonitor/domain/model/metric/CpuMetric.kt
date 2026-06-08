package com.cpumonitor.domain.model.metric

/**
 * Domain model representing a CPU usage snapshot at a point in time.
 */
data class CpuMetric(
    val id: Long = 0L,
    val timestampMillis: Long,
    val totalUsagePercent: Float,
    val perCoreUsagePercent: List<Float>,
    val bigCoreUsagePercent: Float,
    val littleCoreUsagePercent: Float,
    val currentFrequencyMhz: Float,
    val minFrequencyMhz: Float,
    val maxFrequencyMhz: Float,
)
