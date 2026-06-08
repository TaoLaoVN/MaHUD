package com.cpumonitor.domain.usecase.history

import com.cpumonitor.domain.model.history.HistoryMetricType
import com.cpumonitor.domain.model.history.HistoryTimeRange
import com.cpumonitor.domain.repository.MetricsRepository
import com.cpumonitor.domain.usecase.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class ObserveMetricHistoryParams(
    val metricType: HistoryMetricType,
    val timeRange: HistoryTimeRange,
)

/**
 * Immutable history chart point for UI rendering.
 */
data class HistoryChartPoint(
    val timestampMillis: Long,
    val value: Float,
)

/**
 * Streams historical metric samples mapped to chart points.
 */
class ObserveMetricHistoryUseCase @Inject constructor(
    private val metricsRepository: MetricsRepository,
) : FlowUseCase<ObserveMetricHistoryParams, List<HistoryChartPoint>>() {

    override fun execute(params: ObserveMetricHistoryParams): Flow<List<HistoryChartPoint>> {
        val sinceMillis = System.currentTimeMillis() - params.timeRange.durationMillis
        return when (params.metricType) {
            HistoryMetricType.CPU ->
                metricsRepository.observeCpuMetrics(sinceMillis).map { metrics ->
                    metrics.map { HistoryChartPoint(it.timestampMillis, it.totalUsagePercent) }
                }
            HistoryMetricType.MEMORY ->
                metricsRepository.observeMemoryMetrics(sinceMillis).map { metrics ->
                    metrics.map { metric ->
                        val denominator = metric.usedBytes + metric.availableBytes
                        val usedPercent = if (denominator <= 0L) {
                            0f
                        } else {
                            (metric.usedBytes.toFloat() / denominator.toFloat()) * 100f
                        }
                        HistoryChartPoint(metric.timestampMillis, usedPercent)
                    }
                }
            HistoryMetricType.THERMAL ->
                metricsRepository.observeThermalMetrics(sinceMillis).map { metrics ->
                    metrics.map { HistoryChartPoint(it.timestampMillis, it.cpuTemperatureCelsius) }
                }
            HistoryMetricType.BATTERY ->
                metricsRepository.observeBatteryMetrics(sinceMillis).map { metrics ->
                    metrics.map { HistoryChartPoint(it.timestampMillis, it.percentage.toFloat()) }
                }
        }
    }
}
