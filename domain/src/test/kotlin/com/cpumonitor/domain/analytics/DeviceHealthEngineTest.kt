package com.cpumonitor.domain.analytics

import com.cpumonitor.domain.model.analytics.HealthStatus
import com.cpumonitor.domain.model.analytics.MetricSnapshotBundle
import com.cpumonitor.domain.model.metric.BatteryMetric
import com.cpumonitor.domain.model.metric.CpuMetric
import com.cpumonitor.domain.model.metric.MemoryMetric
import com.cpumonitor.domain.model.metric.ThermalMetric
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceHealthEngineTest {

    @Test
    fun evaluate_returnsExcellentScoreForHealthySnapshot() {
        val bundle = MetricSnapshotBundle(
            cpuMetrics = listOf(cpu(usage = 20f)),
            memoryMetrics = listOf(memory(used = 2_000L, available = 8_000L)),
            thermalMetrics = listOf(thermal(cpuTemp = 32f, batteryTemp = 30f)),
            batteryMetrics = listOf(battery(percentage = 90, health = "Good")),
        )

        val report = DeviceHealthEngine.evaluate(bundle, timestampMillis = 1_000L)

        assertTrue(report.overallScore >= 85)
        assertEquals(HealthStatus.EXCELLENT, report.status)
    }

    @Test
    fun evaluate_penalizesHighThermalLoad() {
        val bundle = MetricSnapshotBundle(
            cpuMetrics = listOf(cpu(usage = 95f)),
            memoryMetrics = listOf(memory(used = 9_000L, available = 1_000L)),
            thermalMetrics = listOf(thermal(cpuTemp = 48f, batteryTemp = 46f)),
            batteryMetrics = listOf(battery(percentage = 15, health = "Overheat")),
        )

        val report = DeviceHealthEngine.evaluate(bundle, timestampMillis = 1_000L)

        assertTrue(report.overallScore <= 50)
        assertTrue(
            report.status == HealthStatus.POOR || report.status == HealthStatus.CRITICAL,
        )
    }

    private fun cpu(usage: Float) = CpuMetric(
        timestampMillis = 1_000L,
        totalUsagePercent = usage,
        perCoreUsagePercent = listOf(usage),
        bigCoreUsagePercent = usage,
        littleCoreUsagePercent = usage,
        currentFrequencyMhz = 1_800f,
        minFrequencyMhz = 500f,
        maxFrequencyMhz = 2_000f,
    )

    private fun memory(used: Long, available: Long) = MemoryMetric(
        timestampMillis = 1_000L,
        usedBytes = used,
        availableBytes = available,
        freeBytes = 0L,
        cachedBytes = 0L,
    )

    private fun thermal(cpuTemp: Float, batteryTemp: Float) = ThermalMetric(
        timestampMillis = 1_000L,
        cpuTemperatureCelsius = cpuTemp,
        batteryTemperatureCelsius = batteryTemp,
        thermalZoneTemperatures = emptyMap(),
        isThrottling = cpuTemp >= 45f,
        isOverheating = cpuTemp >= 45f,
    )

    private fun battery(percentage: Int, health: String) = BatteryMetric(
        timestampMillis = 1_000L,
        percentage = percentage,
        voltageMv = 4_000,
        currentMa = -500,
        capacityMah = 4_000,
        temperatureCelsius = 30f,
        health = health,
        isCharging = false,
        chargeSpeedMw = null,
    )
}
