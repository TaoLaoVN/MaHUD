package com.cpumonitor.domain.model.history

/**
 * Metric families available in the history module.
 */
enum class HistoryMetricType(val displayName: String) {
    CPU("CPU"),
    MEMORY("Memory"),
    THERMAL("Thermal"),
    BATTERY("Battery"),
}
