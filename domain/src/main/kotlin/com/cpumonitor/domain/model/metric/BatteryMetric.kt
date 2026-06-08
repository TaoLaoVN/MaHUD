package com.cpumonitor.domain.model.metric

/**
 * Domain model representing battery status at a point in time.
 */
data class BatteryMetric(
    val id: Long = 0L,
    val timestampMillis: Long,
    val percentage: Int,
    val voltageMv: Int,
    val currentMa: Int,
    val capacityMah: Int,
    val temperatureCelsius: Float,
    val health: String,
    val isCharging: Boolean,
    val chargeSpeedMw: Int?,
)
