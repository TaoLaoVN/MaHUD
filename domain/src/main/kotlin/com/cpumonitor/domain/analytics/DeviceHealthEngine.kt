package com.cpumonitor.domain.analytics

import com.cpumonitor.domain.model.analytics.ComponentHealthScore
import com.cpumonitor.domain.model.analytics.DeviceHealthReport
import com.cpumonitor.domain.model.analytics.HealthStatus
import com.cpumonitor.domain.model.analytics.MetricSnapshotBundle
import com.cpumonitor.domain.model.metric.BatteryMetric
import com.cpumonitor.domain.model.metric.CpuMetric
import com.cpumonitor.domain.model.metric.MemoryMetric
import com.cpumonitor.domain.model.metric.ThermalMetric
import kotlin.math.roundToInt

/**
 * Pure device health scoring engine derived from persisted metric history.
 */
object DeviceHealthEngine {

    fun evaluate(bundle: MetricSnapshotBundle, timestampMillis: Long = System.currentTimeMillis()): DeviceHealthReport {
        val components = listOf(
            cpuComponent(bundle.cpuMetrics),
            memoryComponent(bundle.memoryMetrics),
            thermalComponent(bundle.thermalMetrics),
            batteryComponent(bundle.batteryMetrics),
        )

        val overallScore = components.map { it.score }.average().roundToInt().coerceIn(0, 100)

        return DeviceHealthReport(
            overallScore = overallScore,
            status = statusForScore(overallScore),
            components = components,
            timestampMillis = timestampMillis,
        )
    }

    fun statusForScore(score: Int): HealthStatus = when {
        score >= 85 -> HealthStatus.EXCELLENT
        score >= 70 -> HealthStatus.GOOD
        score >= 55 -> HealthStatus.FAIR
        score >= 40 -> HealthStatus.POOR
        else -> HealthStatus.CRITICAL
    }

    private fun cpuComponent(metrics: List<CpuMetric>): ComponentHealthScore {
        if (metrics.isEmpty()) return emptyComponent("CPU", "No CPU samples yet")

        val averageUsage = metrics.map { it.totalUsagePercent }.average().toFloat()
        val score = (100f - averageUsage * 0.85f).roundToInt().coerceIn(0, 100)
        val summary = "Average usage ${averageUsage.roundToInt()}%"

        return ComponentHealthScore(
            component = "CPU",
            score = score,
            status = statusForScore(score),
            summary = summary,
        )
    }

    private fun memoryComponent(metrics: List<MemoryMetric>): ComponentHealthScore {
        if (metrics.isEmpty()) return emptyComponent("Memory", "No memory samples yet")

        val averageUsage = metrics.map(::memoryUsedPercent).average().toFloat()
        val score = (100f - averageUsage * 0.9f).roundToInt().coerceIn(0, 100)
        val summary = "Average usage ${averageUsage.roundToInt()}%"

        return ComponentHealthScore(
            component = "Memory",
            score = score,
            status = statusForScore(score),
            summary = summary,
        )
    }

    private fun thermalComponent(metrics: List<ThermalMetric>): ComponentHealthScore {
        if (metrics.isEmpty()) return emptyComponent("Thermal", "No thermal samples yet")

        val peak = metrics.maxOf { metric ->
            maxOf(metric.cpuTemperatureCelsius, metric.batteryTemperatureCelsius)
        }
        val score = when {
            peak >= 50f -> 20
            peak >= 45f -> 40
            peak >= 40f -> 60
            peak >= 35f -> 80
            else -> 100
        }
        val summary = "Peak temperature ${peak.roundToInt()}°C"

        return ComponentHealthScore(
            component = "Thermal",
            score = score,
            status = statusForScore(score),
            summary = summary,
        )
    }

    private fun batteryComponent(metrics: List<BatteryMetric>): ComponentHealthScore {
        if (metrics.isEmpty()) return emptyComponent("Battery", "No battery samples yet")

        val latest = metrics.maxBy { it.timestampMillis }
        val healthPenalty = when (latest.health.lowercase()) {
            "good" -> 0
            "cold" -> 5
            "dead" -> 100
            "over voltage" -> 30
            "overheat" -> 40
            "unspecified failure" -> 50
            else -> 10
        }
        val score = (
            (latest.percentage.coerceIn(0, 100) * 0.6f) +
                ((100 - healthPenalty) * 0.4f)
            ).roundToInt().coerceIn(0, 100)
        val summary = "${latest.percentage}% • ${latest.health}"

        return ComponentHealthScore(
            component = "Battery",
            score = score,
            status = statusForScore(score),
            summary = summary,
        )
    }

    private fun emptyComponent(name: String, summary: String): ComponentHealthScore =
        ComponentHealthScore(
            component = name,
            score = 50,
            status = HealthStatus.FAIR,
            summary = summary,
        )

    fun memoryUsedPercent(metric: MemoryMetric): Float {
        val totalBytes = metric.usedBytes + metric.availableBytes
        if (totalBytes <= 0L) return 0f
        return ((metric.usedBytes.toDouble() / totalBytes.toDouble()) * 100.0).toFloat()
    }
}
