package com.cpumonitor.domain.model.metric

/**
 * Domain model representing a memory usage snapshot at a point in time.
 */
data class MemoryMetric(
    val id: Long = 0L,
    val timestampMillis: Long,
    val usedBytes: Long,
    val availableBytes: Long,
    val freeBytes: Long,
    val cachedBytes: Long,
)
