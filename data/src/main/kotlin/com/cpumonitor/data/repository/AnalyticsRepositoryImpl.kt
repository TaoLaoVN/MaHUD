package com.cpumonitor.data.repository

import com.cpumonitor.core.common.dispatcher.DispatchersProvider
import com.cpumonitor.data.util.monitoringPollFlow
import com.cpumonitor.domain.analytics.AdvancedAnalyticsEngine
import com.cpumonitor.domain.analytics.DeviceHealthEngine
import com.cpumonitor.domain.analytics.PerformanceScoreEngine
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.analytics.AnalyticsDashboard
import com.cpumonitor.domain.model.analytics.DeviceHealthReport
import com.cpumonitor.domain.model.analytics.MetricSnapshotBundle
import com.cpumonitor.domain.model.history.HistoryTimeRange
import com.cpumonitor.domain.model.metric.BatteryMetric
import com.cpumonitor.domain.model.metric.CpuMetric
import com.cpumonitor.domain.model.metric.MemoryMetric
import com.cpumonitor.domain.model.metric.ThermalMetric
import com.cpumonitor.domain.repository.AnalyticsRepository
import com.cpumonitor.domain.repository.MetricsRepository
import com.cpumonitor.domain.repository.MonitoringConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val metricsRepository: MetricsRepository,
    dispatchersProvider: DispatchersProvider,
) : BaseRepository(dispatchersProvider.io), AnalyticsRepository {

    override fun observeAnalyticsDashboard(
        intervalMs: Long,
        window: HistoryTimeRange,
    ): Flow<Result<AnalyticsDashboard>> = observeSafely {
        monitoringPollFlow(intervalMs.coerceAtLeast(MonitoringConstants.MIN_REFRESH_INTERVAL_MS)) {
            buildDashboard(window)
        }
            .distinctUntilChangedBy { dashboard -> dashboard.health.overallScore to dashboard.performance.overallScore }
            .flowOn(dispatcher)
    }

    override fun observeDeviceHealth(
        intervalMs: Long,
        window: HistoryTimeRange,
    ): Flow<Result<DeviceHealthReport>> = observeSafely {
        monitoringPollFlow(intervalMs.coerceAtLeast(MonitoringConstants.MIN_REFRESH_INTERVAL_MS)) {
            buildDashboard(window).health
        }
            .distinctUntilChangedBy { health -> health.overallScore to health.status }
            .flowOn(dispatcher)
    }

    override suspend fun getAnalyticsDashboard(window: HistoryTimeRange): Result<AnalyticsDashboard> =
        safeCall { buildDashboard(window) }

    private suspend fun buildDashboard(window: HistoryTimeRange): AnalyticsDashboard {
        val bundle = loadMetricBundle(window.durationMillis)
        val timestampMillis = System.currentTimeMillis()

        return AnalyticsDashboard(
            health = DeviceHealthEngine.evaluate(bundle, timestampMillis),
            performance = PerformanceScoreEngine.evaluate(bundle),
            analytics = AdvancedAnalyticsEngine.evaluate(bundle, window.durationMillis),
        )
    }

    private suspend fun loadMetricBundle(windowMillis: Long): MetricSnapshotBundle {
        val sinceMillis = System.currentTimeMillis() - windowMillis

        return MetricSnapshotBundle(
            cpuMetrics = unwrapMetricList(metricsRepository.getCpuMetrics(sinceMillis)),
            memoryMetrics = unwrapMetricList(metricsRepository.getMemoryMetrics(sinceMillis)),
            thermalMetrics = unwrapMetricList(metricsRepository.getThermalMetrics(sinceMillis)),
            batteryMetrics = unwrapMetricList(metricsRepository.getBatteryMetrics(sinceMillis)),
        )
    }

    private fun <T> unwrapMetricList(result: Result<List<T>>): List<T> = when (result) {
        is Result.Success -> result.data
        else -> emptyList()
    }
}
