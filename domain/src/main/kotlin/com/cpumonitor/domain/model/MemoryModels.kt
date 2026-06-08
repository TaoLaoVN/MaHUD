package com.cpumonitor.domain.model

/**
 * System memory snapshot sourced from [android.app.ActivityManager.MemoryInfo].
 */
data class MemoryMetrics(
    val timestampMillis: Long,
    val totalBytes: Long,
    val usedBytes: Long,
    val availableBytes: Long,
    val freeBytes: Long,
    val cachedBytes: Long,
    val isLowMemory: Boolean,
    val lowMemoryThresholdBytes: Long,
)
