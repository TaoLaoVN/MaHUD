package com.cpumonitor.domain.analytics

import com.cpumonitor.domain.model.analytics.MetricSnapshotBundle
import com.cpumonitor.domain.model.analytics.TrendDirection
import com.cpumonitor.domain.model.metric.BatteryMetric
import com.cpumonitor.domain.model.metric.CpuMetric
import com.cpumonitor.domain.model.metric.MemoryMetric
import com.cpumonitor.domain.model.metric.ThermalMetric
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdvancedAnalyticsEngineTest {

    @Test
    fun evaluate_detectsRisingCpuTrend() {
        val now = System.currentTimeMillis()
        val bundle = MetricSnapshotBundle(
            cpuMetrics = listOf(
                cpu(totalUsage = 30f, timestamp = now - 90 * 60_000L),
                cpu(totalUsage = 35f, timestamp = now - 75 * 60_000L),
                cpu(totalUsage = 70f, timestamp = now - 15 * 60_000L),
                cpu(totalUsage = 85f, timestamp = now - 5 * 60_000L),
            ),
            memoryMetrics = listOf(memory(used = 4_000L, available = 6_000L, timestamp = now)),
            thermalMetrics = listOf(thermal(cpuTemp = 36f, batteryTemp = 34f, timestamp = now)),
            batteryMetrics = listOf(battery(percentage = 80, timestamp = now)),
        )

        val snapshot = AdvancedAnalyticsEngine.evaluate(bundle, windowMillis = 3_600_000L)
        val cpuTrend = snapshot.trends.first { it.metricName == "CPU usage" }

        assertEquals(TrendDirection.UP, cpuTrend.direction)
        assertTrue(snapshot.insights.isNotEmpty())
    }

    private fun cpu(totalUsage: Float, timestamp: Long) = CpuMetric(
        timestampMillis = timestamp,
        totalUsagePercent = totalUsage,
        perCoreUsagePercent = listOf(totalUsage),
        bigCoreUsagePercent = totalUsage,
        littleCoreUsagePercent = totalUsage,
        currentFrequencyMhz = 1_800f,
        minFrequencyMhz = 300f,
        maxFrequencyMhz = 2_400f,
    )

    private fun memory(used: Long, available: Long, timestamp: Long) = MemoryMetric(
        timestampMillis = timestamp,
        usedBytes = used,
        availableBytes = available,
        freeBytes = available / 2,
        cachedBytes = available / 4,
    )

    private fun thermal(cpuTemp: Float, batteryTemp: Float, timestamp: Long) = ThermalMetric(
        timestampMillis = timestamp,
        cpuTemperatureCelsius = cpuTemp,
        batteryTemperatureCelsius = batteryTemp,
        thermalZoneTemperatures = emptyMap(),
        isThrottling = cpuTemp >= 45f,
        isOverheating = cpuTemp > 45f,
    )

    private fun battery(percentage: Int, timestamp: Long) = BatteryMetric(
        timestampMillis = timestamp,
        percentage = percentage,
        voltageMv = 4_100,
        currentMa = -500,
        capacityMah = 4_000,
        temperatureCelsius = 32f,
        health = "Good",
        isCharging = false,
        chargeSpeedMw = null,
    )
}
