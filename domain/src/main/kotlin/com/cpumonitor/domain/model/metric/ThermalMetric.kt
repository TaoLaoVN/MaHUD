package com.cpumonitor.domain.model.metric

/**
 * Domain model representing thermal readings at a point in time.
 */
data class ThermalMetric(
    val id: Long = 0L,
    val timestampMillis: Long,
    val cpuTemperatureCelsius: Float,
    val batteryTemperatureCelsius: Float,
    val thermalZoneTemperatures: Map<String, Float>,
    val isThrottling: Boolean,
    val isOverheating: Boolean,
)
