package com.cpumonitor.domain.model

/**
 * Realtime battery snapshot sourced from [android.os.BatteryManager].
 * Distinct from [com.cpumonitor.domain.model.metric.BatteryMetric] which is used for persistence.
 */
data class BatteryMetrics(
    val timestampMillis: Long,
    val percentage: Int,
    val voltageMv: Int,
    val currentMa: Int,
    val temperatureCelsius: Float,
    val health: String,
    val isCharging: Boolean,
    val chargeSpeedMw: Int?,
)
