package com.cpumonitor.data.datasource.export

import org.junit.Assert.assertTrue
import org.junit.Test

class MetricsReportBuilderTest {

    @Test
    fun buildCsv_containsMetricSections() {
        val snapshot = MetricsReportSnapshot(
            cpuMetrics = emptyList(),
            memoryMetrics = emptyList(),
            thermalMetrics = emptyList(),
            batteryMetrics = emptyList(),
            sinceMillis = 1_000L,
            generatedAtMillis = 2_000L,
        )

        val csv = MetricsReportBuilder.buildCsv(snapshot)

        assertTrue(csv.contains("CPU Monitor Diagnostic Report"))
        assertTrue(csv.contains("cpu_timestamp"))
        assertTrue(csv.contains("memory_timestamp"))
        assertTrue(csv.contains("thermal_timestamp"))
        assertTrue(csv.contains("battery_timestamp"))
    }

    @Test
    fun buildJson_containsMetricArrays() {
        val snapshot = MetricsReportSnapshot(
            cpuMetrics = emptyList(),
            memoryMetrics = emptyList(),
            thermalMetrics = emptyList(),
            batteryMetrics = emptyList(),
            sinceMillis = 1_000L,
            generatedAtMillis = 2_000L,
        )

        val json = MetricsReportBuilder.buildJson(snapshot)

        assertTrue(json.contains("\"cpu\""))
        assertTrue(json.contains("\"memory\""))
        assertTrue(json.contains("\"thermal\""))
        assertTrue(json.contains("\"battery\""))
    }
}
