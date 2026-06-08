package com.cpumonitor.domain.model

/**
 * Real-time metrics displayed on the floating overlay monitor.
 */
data class OverlayMetrics(
    val cpuUsagePercent: Float,
    val ramUsagePercent: Float,
    val temperatureCelsius: Float,
    val fps: Int,
)
