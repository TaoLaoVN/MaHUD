package com.cpumonitor.domain.alert

import com.cpumonitor.domain.model.alert.AlertMetricType
import com.cpumonitor.domain.model.alert.AlertRule

/**
 * Pure alert threshold evaluation logic.
 */
object AlertEvaluator {

    fun metricValue(
        rule: AlertRule,
        cpuUsagePercent: Float,
        memoryUsagePercent: Float,
        thermalCelsius: Float,
        batteryPercent: Float,
    ): Float = when (rule.metricType) {
        AlertMetricType.CPU -> cpuUsagePercent
        AlertMetricType.MEMORY -> memoryUsagePercent
        AlertMetricType.THERMAL -> thermalCelsius
        AlertMetricType.BATTERY -> batteryPercent
    }

    fun isTriggered(
        rule: AlertRule,
        cpuUsagePercent: Float,
        memoryUsagePercent: Float,
        thermalCelsius: Float,
        batteryPercent: Float,
    ): Boolean {
        if (!rule.enabled) return false
        val value = metricValue(rule, cpuUsagePercent, memoryUsagePercent, thermalCelsius, batteryPercent)
        return rule.matches(value)
    }
}
