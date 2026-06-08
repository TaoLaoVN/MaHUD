package com.cpumonitor.domain.analytics

import com.cpumonitor.domain.model.analytics.MetricSnapshotBundle
import com.cpumonitor.domain.model.analytics.PerformanceScore
import com.cpumonitor.domain.model.metric.BatteryMetric
import com.cpumonitor.domain.model.metric.CpuMetric
import com.cpumonitor.domain.model.metric.MemoryMetric
import com.cpumonitor.domain.model.metric.ThermalMetric
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Performance scoring engine based on metric stability and headroom.
 */
object PerformanceScoreEngine {

    fun evaluate(bundle: MetricSnapshotBundle): PerformanceScore {
        val cpuStabilityScore = cpuStabilityScore(bundle.cpuMetrics)
        val thermalHeadroomScore = thermalHeadroomScore(bundle.thermalMetrics)
        val memoryEfficiencyScore = memoryEfficiencyScore(bundle.memoryMetrics)
        val batteryEnduranceScore = batteryEnduranceScore(bundle.batteryMetrics)

        val overallScore = listOf(
            cpuStabilityScore,
            thermalHeadroomScore,
            memoryEfficiencyScore,
            batteryEnduranceScore,
        ).average().roundToInt().coerceIn(0, 100)

        val summary = when {
            overallScore >= 85 -> "System performance is stable with healthy thermal headroom."
            overallScore >= 70 -> "Performance is good with minor variability."
            overallScore >= 55 -> "Performance is acceptable but shows some stress."
            else -> "Performance is constrained by sustained load or heat."
        }

        return PerformanceScore(
            overallScore = overallScore,
            cpuStabilityScore = cpuStabilityScore,
            thermalHeadroomScore = thermalHeadroomScore,
            memoryEfficiencyScore = memoryEfficiencyScore,
            batteryEnduranceScore = batteryEnduranceScore,
            summary = summary,
        )
    }

    private fun cpuStabilityScore(metrics: List<CpuMetric>): Int {
        if (metrics.size < 2) return 50
        val values = metrics.map { it.totalUsagePercent }
        val mean = values.average().toFloat()
        if (mean <= 0f) return 100

        val variance = values.map { value -> (value - mean) * (value - mean) }.average()
        val coefficientOfVariation = sqrt(variance).toFloat() / mean
        return ((1f - coefficientOfVariation.coerceIn(0f, 1f)) * 100f).roundToInt().coerceIn(0, 100)
    }

    private fun thermalHeadroomScore(metrics: List<ThermalMetric>): Int {
        if (metrics.isEmpty()) return 50
        val peak = metrics.maxOf { maxOf(it.cpuTemperatureCelsius, it.batteryTemperatureCelsius) }
        val headroom = (50f - peak).coerceAtLeast(0f)
        return ((headroom / 50f) * 100f).roundToInt().coerceIn(0, 100)
    }

    private fun memoryEfficiencyScore(metrics: List<MemoryMetric>): Int {
        if (metrics.isEmpty()) return 50
        val averageUsage = metrics.map(DeviceHealthEngine::memoryUsedPercent).average().toFloat()
        return (100f - averageUsage * 0.85f).roundToInt().coerceIn(0, 100)
    }

    private fun batteryEnduranceScore(metrics: List<BatteryMetric>): Int {
        if (metrics.isEmpty()) return 50
        val latest = metrics.maxBy { it.timestampMillis }
        val dischargePenalty = if (!latest.isCharging && latest.currentMa < 0) {
            (kotlin.math.abs(latest.currentMa) / 100f).coerceAtMost(30f)
        } else {
            0f
        }
        return (latest.percentage - dischargePenalty).roundToInt().coerceIn(0, 100)
    }
}
