package com.cpumonitor.domain.analytics

import com.cpumonitor.domain.model.analytics.AdvancedAnalyticsSnapshot
import com.cpumonitor.domain.model.analytics.AnalyticsInsight
import com.cpumonitor.domain.model.analytics.HealthStatus
import com.cpumonitor.domain.model.analytics.MetricSnapshotBundle
import com.cpumonitor.domain.model.analytics.MetricTrend
import com.cpumonitor.domain.model.analytics.TrendDirection
import com.cpumonitor.domain.model.metric.BatteryMetric
import com.cpumonitor.domain.model.metric.CpuMetric
import com.cpumonitor.domain.model.metric.MemoryMetric
import com.cpumonitor.domain.model.metric.ThermalMetric
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Advanced analytics engine for trend detection and insight generation.
 */
object AdvancedAnalyticsEngine {

    private const val TREND_WINDOW_MILLIS = 60 * 60_000L
    private const val STABLE_THRESHOLD_PERCENT = 5f

    fun evaluate(bundle: MetricSnapshotBundle, windowMillis: Long): AdvancedAnalyticsSnapshot {
        val now = System.currentTimeMillis()
        val recentStart = now - TREND_WINDOW_MILLIS
        val previousStart = now - (TREND_WINDOW_MILLIS * 2)

        val recentCpu = bundle.cpuMetrics.filter { it.timestampMillis >= recentStart }
        val previousCpu = bundle.cpuMetrics.filter { it.timestampMillis in previousStart until recentStart }
        val recentMemory = bundle.memoryMetrics.filter { it.timestampMillis >= recentStart }
        val previousMemory = bundle.memoryMetrics.filter { it.timestampMillis in previousStart until recentStart }
        val recentThermal = bundle.thermalMetrics.filter { it.timestampMillis >= recentStart }
        val previousThermal = bundle.thermalMetrics.filter { it.timestampMillis in previousStart until recentStart }

        val trends = listOf(
            buildTrend(
                metricName = "CPU usage",
                currentValues = recentCpu.map { it.totalUsagePercent },
                previousValues = previousCpu.map { it.totalUsagePercent },
            ),
            buildTrend(
                metricName = "Memory usage",
                currentValues = recentMemory.map(DeviceHealthEngine::memoryUsedPercent),
                previousValues = previousMemory.map(DeviceHealthEngine::memoryUsedPercent),
            ),
            buildTrend(
                metricName = "Temperature",
                currentValues = recentThermal.map { maxOf(it.cpuTemperatureCelsius, it.batteryTemperatureCelsius) },
                previousValues = previousThermal.map { maxOf(it.cpuTemperatureCelsius, it.batteryTemperatureCelsius) },
            ),
        )

        val peakCpu = bundle.cpuMetrics.maxOfOrNull { it.totalUsagePercent } ?: 0f
        val peakMemory = bundle.memoryMetrics.maxOfOrNull(DeviceHealthEngine::memoryUsedPercent) ?: 0f
        val peakTemperature = bundle.thermalMetrics.maxOfOrNull {
            maxOf(it.cpuTemperatureCelsius, it.batteryTemperatureCelsius)
        } ?: 0f

        val sampleCount = bundle.cpuMetrics.size +
            bundle.memoryMetrics.size +
            bundle.thermalMetrics.size +
            bundle.batteryMetrics.size

        return AdvancedAnalyticsSnapshot(
            trends = trends,
            insights = buildInsights(trends, peakCpu, peakMemory, peakTemperature, bundle),
            peakCpuPercent = peakCpu,
            peakMemoryPercent = peakMemory,
            peakTemperatureCelsius = peakTemperature,
            sampleCount = sampleCount,
            windowMillis = windowMillis,
        )
    }

    private fun buildTrend(
        metricName: String,
        currentValues: List<Float>,
        previousValues: List<Float>,
    ): MetricTrend {
        val currentAverage = currentValues.averageOrZero()
        val previousAverage = previousValues.averageOrZero()
        val changePercent = if (previousAverage <= 0f) {
            0f
        } else {
            ((currentAverage - previousAverage) / previousAverage) * 100f
        }

        val direction = when {
            abs(changePercent) < STABLE_THRESHOLD_PERCENT -> TrendDirection.STABLE
            changePercent > 0f -> TrendDirection.UP
            else -> TrendDirection.DOWN
        }

        return MetricTrend(
            metricName = metricName,
            direction = direction,
            changePercent = changePercent,
            currentAverage = currentAverage,
            previousAverage = previousAverage,
        )
    }

    private fun buildInsights(
        trends: List<MetricTrend>,
        peakCpu: Float,
        peakMemory: Float,
        peakTemperature: Float,
        bundle: MetricSnapshotBundle,
    ): List<AnalyticsInsight> {
        val insights = mutableListOf<AnalyticsInsight>()

        trends.forEach { trend ->
            if (trend.direction == TrendDirection.UP && abs(trend.changePercent) >= STABLE_THRESHOLD_PERCENT) {
                insights += AnalyticsInsight(
                    title = "${trend.metricName} rising",
                    description = "${trend.metricName} increased ${trend.changePercent.roundToInt()}% in the last hour.",
                    severity = when (trend.metricName) {
                        "Temperature" -> HealthStatus.POOR
                        "CPU usage" -> HealthStatus.FAIR
                        else -> HealthStatus.GOOD
                    },
                )
            }
        }

        if (peakCpu >= 90f) {
            insights += AnalyticsInsight(
                title = "High CPU load detected",
                description = "Peak CPU usage reached ${peakCpu.roundToInt()}% in the selected window.",
                severity = HealthStatus.POOR,
            )
        }

        if (peakMemory >= 85f) {
            insights += AnalyticsInsight(
                title = "Memory pressure detected",
                description = "Peak memory usage reached ${peakMemory.roundToInt()}%.",
                severity = HealthStatus.FAIR,
            )
        }

        if (peakTemperature >= 45f) {
            insights += AnalyticsInsight(
                title = "Elevated temperature",
                description = "Peak temperature reached ${peakTemperature.roundToInt()}°C.",
                severity = HealthStatus.POOR,
            )
        }

        val throttlingEvents = bundle.thermalMetrics.count { it.isThrottling }
        if (throttlingEvents > 0) {
            insights += AnalyticsInsight(
                title = "Thermal throttling observed",
                description = "$throttlingEvents thermal throttling events recorded.",
                severity = HealthStatus.CRITICAL,
            )
        }

        if (insights.isEmpty()) {
            insights += AnalyticsInsight(
                title = "System stable",
                description = "No significant anomalies detected in the selected analytics window.",
                severity = HealthStatus.EXCELLENT,
            )
        }

        return insights
    }

    private fun List<Float>.averageOrZero(): Float =
        if (isEmpty()) 0f else average().toFloat()
}
