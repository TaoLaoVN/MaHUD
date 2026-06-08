package com.cpumonitor.domain.alert

import com.cpumonitor.domain.model.alert.AlertComparator
import com.cpumonitor.domain.model.alert.AlertMetricType
import com.cpumonitor.domain.model.alert.AlertRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertEvaluatorTest {

    private val cpuRule = AlertRule(
        id = "cpu_high",
        metricType = AlertMetricType.CPU,
        threshold = 90f,
        comparator = AlertComparator.GREATER_THAN,
        label = "CPU above 90%",
    )

    private val batteryRule = AlertRule(
        id = "battery_low",
        metricType = AlertMetricType.BATTERY,
        threshold = 20f,
        comparator = AlertComparator.LESS_THAN,
        label = "Battery below 20%",
    )

    @Test
    fun metricValue_returnsCorrectMetric() {
        assertEquals(75f, AlertEvaluator.metricValue(cpuRule, 75f, 50f, 40f, 80f))
        assertEquals(15f, AlertEvaluator.metricValue(batteryRule, 75f, 50f, 40f, 15f))
    }

    @Test
    fun isTriggered_respectsThresholdAndComparator() {
        assertTrue(
            AlertEvaluator.isTriggered(cpuRule, cpuUsagePercent = 95f, memoryUsagePercent = 0f, thermalCelsius = 0f, batteryPercent = 100f),
        )
        assertFalse(
            AlertEvaluator.isTriggered(cpuRule, cpuUsagePercent = 80f, memoryUsagePercent = 0f, thermalCelsius = 0f, batteryPercent = 100f),
        )
        assertTrue(
            AlertEvaluator.isTriggered(batteryRule, cpuUsagePercent = 0f, memoryUsagePercent = 0f, thermalCelsius = 0f, batteryPercent = 10f),
        )
    }

    @Test
    fun isTriggered_returnsFalseWhenRuleDisabled() {
        val disabled = cpuRule.copy(enabled = false)
        assertFalse(
            AlertEvaluator.isTriggered(disabled, cpuUsagePercent = 99f, memoryUsagePercent = 0f, thermalCelsius = 0f, batteryPercent = 100f),
        )
    }
}
