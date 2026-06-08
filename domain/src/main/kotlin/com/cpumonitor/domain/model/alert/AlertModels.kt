package com.cpumonitor.domain.model.alert

enum class AlertMetricType(val displayName: String) {
    CPU("CPU"),
    MEMORY("Memory"),
    THERMAL("Thermal"),
    BATTERY("Battery"),
}

enum class AlertComparator {
    GREATER_THAN,
    LESS_THAN,
}

/**
 * User-defined alert rule persisted locally.
 */
data class AlertRule(
    val id: String,
    val metricType: AlertMetricType,
    val threshold: Float,
    val comparator: AlertComparator,
    val enabled: Boolean = true,
    val label: String,
) {
    fun matches(value: Float): Boolean = when (comparator) {
        AlertComparator.GREATER_THAN -> value > threshold
        AlertComparator.LESS_THAN -> value < threshold
    }
}

/**
 * Historical record of a fired alert.
 */
data class AlertHistoryEntry(
    val id: Long = 0L,
    val ruleId: String,
    val message: String,
    val metricValue: Float,
    val timestampMillis: Long,
)

object DefaultAlertRules {
    val presets: List<AlertRule> = listOf(
        AlertRule(
            id = "cpu_high",
            metricType = AlertMetricType.CPU,
            threshold = 90f,
            comparator = AlertComparator.GREATER_THAN,
            label = "CPU above 90%",
        ),
        AlertRule(
            id = "thermal_high",
            metricType = AlertMetricType.THERMAL,
            threshold = 45f,
            comparator = AlertComparator.GREATER_THAN,
            label = "Temperature above 45°C",
        ),
        AlertRule(
            id = "memory_high",
            metricType = AlertMetricType.MEMORY,
            threshold = 85f,
            comparator = AlertComparator.GREATER_THAN,
            label = "RAM above 85%",
        ),
        AlertRule(
            id = "battery_low",
            metricType = AlertMetricType.BATTERY,
            threshold = 20f,
            comparator = AlertComparator.LESS_THAN,
            label = "Battery below 20%",
        ),
    )
}
