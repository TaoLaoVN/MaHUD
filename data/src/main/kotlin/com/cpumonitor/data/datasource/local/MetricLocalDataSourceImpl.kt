package com.cpumonitor.data.datasource.local

import com.cpumonitor.core.database.dao.BatteryMetricsDao
import com.cpumonitor.core.database.dao.CpuMetricsDao
import com.cpumonitor.core.database.dao.MemoryMetricsDao
import com.cpumonitor.core.database.dao.ThermalMetricsDao
import com.cpumonitor.data.mapper.toDomain
import com.cpumonitor.data.mapper.toEntity
import com.cpumonitor.domain.model.metric.BatteryMetric
import com.cpumonitor.domain.model.metric.CpuMetric
import com.cpumonitor.domain.model.metric.MemoryMetric
import com.cpumonitor.domain.model.metric.ThermalMetric
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MetricLocalDataSourceImpl @Inject constructor(
    private val cpuMetricsDao: CpuMetricsDao,
    private val memoryMetricsDao: MemoryMetricsDao,
    private val thermalMetricsDao: ThermalMetricsDao,
    private val batteryMetricsDao: BatteryMetricsDao,
) : MetricLocalDataSource {

    override fun observeCpuMetrics(sinceMillis: Long): Flow<List<CpuMetric>> =
        cpuMetricsDao.observeSince(sinceMillis).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeMemoryMetrics(sinceMillis: Long): Flow<List<MemoryMetric>> =
        memoryMetricsDao.observeSince(sinceMillis).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeThermalMetrics(sinceMillis: Long): Flow<List<ThermalMetric>> =
        thermalMetricsDao.observeSince(sinceMillis).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeBatteryMetrics(sinceMillis: Long): Flow<List<BatteryMetric>> =
        batteryMetricsDao.observeSince(sinceMillis).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getCpuMetrics(sinceMillis: Long): List<CpuMetric> =
        cpuMetricsDao.getSince(sinceMillis).map { it.toDomain() }

    override suspend fun getMemoryMetrics(sinceMillis: Long): List<MemoryMetric> =
        memoryMetricsDao.getSince(sinceMillis).map { it.toDomain() }

    override suspend fun getThermalMetrics(sinceMillis: Long): List<ThermalMetric> =
        thermalMetricsDao.getSince(sinceMillis).map { it.toDomain() }

    override suspend fun getBatteryMetrics(sinceMillis: Long): List<BatteryMetric> =
        batteryMetricsDao.getSince(sinceMillis).map { it.toDomain() }

    override suspend fun insertCpuMetric(metric: CpuMetric): Long =
        cpuMetricsDao.insert(metric.toEntity())

    override suspend fun insertMemoryMetric(metric: MemoryMetric): Long =
        memoryMetricsDao.insert(metric.toEntity())

    override suspend fun insertThermalMetric(metric: ThermalMetric): Long =
        thermalMetricsDao.insert(metric.toEntity())

    override suspend fun insertBatteryMetric(metric: BatteryMetric): Long =
        batteryMetricsDao.insert(metric.toEntity())

    override suspend fun deleteCpuMetricsBefore(timestampMillis: Long) {
        cpuMetricsDao.deleteBefore(timestampMillis)
    }

    override suspend fun deleteMemoryMetricsBefore(timestampMillis: Long) {
        memoryMetricsDao.deleteBefore(timestampMillis)
    }

    override suspend fun deleteThermalMetricsBefore(timestampMillis: Long) {
        thermalMetricsDao.deleteBefore(timestampMillis)
    }

    override suspend fun deleteBatteryMetricsBefore(timestampMillis: Long) {
        batteryMetricsDao.deleteBefore(timestampMillis)
    }
}
