package com.cpumonitor.domain.model

/**
 * Realtime thermal snapshot for overlay and dashboard monitoring.
 */
data class ThermalMetrics(
    val timestampMillis: Long,
    val cpuTemperatureCelsius: Float,
    val batteryTemperatureCelsius: Float,
) {
    /** Best available temperature for compact overlay display. */
    val primaryTemperatureCelsius: Float
        get() = when {
            cpuTemperatureCelsius > 0f -> cpuTemperatureCelsius
            batteryTemperatureCelsius > 0f -> batteryTemperatureCelsius
            else -> 0f
        }
}
