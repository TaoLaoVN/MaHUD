package com.cpumonitor.data.datasource.local

import com.cpumonitor.domain.model.alert.AlertComparator
import com.cpumonitor.domain.model.alert.AlertMetricType
import com.cpumonitor.domain.model.alert.AlertRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertRulesCodecTest {

    @Test
    fun encodeAndDecode_roundTripsRules() {
        val rules = listOf(
            AlertRule(
                id = "cpu_high",
                metricType = AlertMetricType.CPU,
                threshold = 90f,
                comparator = AlertComparator.GREATER_THAN,
                enabled = true,
                label = "CPU above 90%",
            ),
            AlertRule(
                id = "battery_low",
                metricType = AlertMetricType.BATTERY,
                threshold = 20f,
                comparator = AlertComparator.LESS_THAN,
                enabled = false,
                label = "Battery below 20%",
            ),
        )

        val encoded = AlertRulesCodec.encode(rules)
        val decoded = AlertRulesCodec.decode(encoded)

        assertEquals(rules, decoded)
    }

    @Test
    fun decode_returnsEmptyListForBlankInput() {
        assertTrue(AlertRulesCodec.decode("").isEmpty())
        assertTrue(AlertRulesCodec.decode("   ").isEmpty())
    }
}
