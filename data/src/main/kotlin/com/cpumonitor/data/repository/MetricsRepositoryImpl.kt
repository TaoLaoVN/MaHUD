package com.cpumonitor.data.repository

import com.cpumonitor.core.common.dispatcher.DispatchersProvider
import com.cpumonitor.data.datasource.local.MetricLocalDataSource
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.metric.BatteryMetric
import com.cpumonitor.domain.model.metric.CpuMetric
import com.cpumonitor.domain.model.metric.MemoryMetric
import com.cpumonitor.domain.model.metric.ThermalMetric
import com.cpumonitor.domain.repository.MetricsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetricsRepositoryImpl @Inject constructor(
    dispatchersProvider: DispatchersProvider,
    private val metricLocalDataSource: MetricLocalDataSource,
) : BaseRepository(dispatchersProvider.io), MetricsRepository {

    override fun observeCpuMetrics(sinceMillis: Long): Flow<List<CpuMetric>> =
        metricLocalDataSource.observeCpuMetrics(sinceMillis)

    override fun observeMemoryMetrics(sinceMillis: Long): Flow<List<MemoryMetric>> =
        metricLocalDataSource.observeMemoryMetrics(sinceMillis)

    override fun observeThermalMetrics(sinceMillis: Long): Flow<List<ThermalMetric>> =
        metricLocalDataSource.observeThermalMetrics(sinceMillis)

    override fun observeBatteryMetrics(sinceMillis: Long): Flow<List<BatteryMetric>> =
        metricLocalDataSource.observeBatteryMetrics(sinceMillis)

    override suspend fun getCpuMetrics(sinceMillis: Long): Result<List<CpuMetric>> =
        safeCall { metricLocalDataSource.getCpuMetrics(sinceMillis) }

    override suspend fun getMemoryMetrics(sinceMillis: Long): Result<List<MemoryMetric>> =
        safeCall { metricLocalDataSource.getMemoryMetrics(sinceMillis) }

    override suspend fun getThermalMetrics(sinceMillis: Long): Result<List<ThermalMetric>> =
        safeCall { metricLocalDataSource.getThermalMetrics(sinceMillis) }

    override suspend fun getBatteryMetrics(sinceMillis: Long): Result<List<BatteryMetric>> =
        safeCall { metricLocalDataSource.getBatteryMetrics(sinceMillis) }

    override suspend fun insertCpuMetric(metric: CpuMetric): Result<Long> =
        safeCall { metricLocalDataSource.insertCpuMetric(metric) }

    override suspend fun insertMemoryMetric(metric: MemoryMetric): Result<Long> =
        safeCall { metricLocalDataSource.insertMemoryMetric(metric) }

    override suspend fun insertThermalMetric(metric: ThermalMetric): Result<Long> =
        safeCall { metricLocalDataSource.insertThermalMetric(metric) }

    override suspend fun insertBatteryMetric(metric: BatteryMetric): Result<Long> =
        safeCall { metricLocalDataSource.insertBatteryMetric(metric) }

    override suspend fun deleteCpuMetricsBefore(timestampMillis: Long): Result<Unit> =
        safeCall { metricLocalDataSource.deleteCpuMetricsBefore(timestampMillis) }

    override suspend fun deleteMemoryMetricsBefore(timestampMillis: Long): Result<Unit> =
        safeCall { metricLocalDataSource.deleteMemoryMetricsBefore(timestampMillis) }

    override suspend fun deleteThermalMetricsBefore(timestampMillis: Long): Result<Unit> =
        safeCall { metricLocalDataSource.deleteThermalMetricsBefore(timestampMillis) }

    override suspend fun deleteBatteryMetricsBefore(timestampMillis: Long): Result<Unit> =
        safeCall { metricLocalDataSource.deleteBatteryMetricsBefore(timestampMillis) }
}
