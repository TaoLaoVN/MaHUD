package com.cpumonitor.domain.model.settings

/**
 * Per-metric retention configuration.
 */
data class RetentionPolicy(
    val cpuRetention: RetentionPeriod = RetentionPeriod.DAYS_7,
    val memoryRetention: RetentionPeriod = RetentionPeriod.DAYS_7,
    val thermalRetention: RetentionPeriod = RetentionPeriod.DAYS_7,
    val batteryRetention: RetentionPeriod = RetentionPeriod.DAYS_7,
)
