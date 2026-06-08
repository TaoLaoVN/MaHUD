package com.cpumonitor.domain.model

/**
 * Snapshot of CPU utilization derived from consecutive `/proc/stat` samples.
 */
data class CpuUsageMetrics(
    val timestampMillis: Long,
    val totalUsagePercent: Float,
    val perCoreUsagePercent: List<Float>,
)

/**
 * Static CPU hardware and architecture information from `/proc/cpuinfo`.
 */
data class CpuArchitectureInfo(
    val coreCount: Int,
    val processors: List<CpuProcessorInfo>,
    val abi: String,
    val hardware: String?,
)

/**
 * Per-logical-processor details parsed from `/proc/cpuinfo`.
 */
data class CpuProcessorInfo(
    val index: Int,
    val modelName: String?,
    val currentFrequencyMhz: Float?,
    val maxFrequencyMhz: Float?,
    val implementer: String?,
    val architecture: String?,
    val variant: String?,
    val part: String?,
)
