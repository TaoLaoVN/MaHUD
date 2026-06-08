package com.cpumonitor.data.datasource.local

import com.cpumonitor.data.datasource.LocalDataSource
import com.cpumonitor.domain.model.metric.BatteryMetric
import com.cpumonitor.domain.model.metric.CpuMetric
import com.cpumonitor.domain.model.metric.MemoryMetric
import com.cpumonitor.domain.model.metric.ThermalMetric
import kotlinx.coroutines.flow.Flow

/**
 * Local persistence contract for historical metrics stored in Room.
 */
interface MetricLocalDataSource : LocalDataSource {

    fun observeCpuMetrics(sinceMillis: Long): Flow<List<CpuMetric>>

    fun observeMemoryMetrics(sinceMillis: Long): Flow<List<MemoryMetric>>

    fun observeThermalMetrics(sinceMillis: Long): Flow<List<ThermalMetric>>

    fun observeBatteryMetrics(sinceMillis: Long): Flow<List<BatteryMetric>>

    suspend fun getCpuMetrics(sinceMillis: Long): List<CpuMetric>

    suspend fun getMemoryMetrics(sinceMillis: Long): List<MemoryMetric>

    suspend fun getThermalMetrics(sinceMillis: Long): List<ThermalMetric>

    suspend fun getBatteryMetrics(sinceMillis: Long): List<BatteryMetric>

    suspend fun insertCpuMetric(metric: CpuMetric): Long

    suspend fun insertMemoryMetric(metric: MemoryMetric): Long

    suspend fun insertThermalMetric(metric: ThermalMetric): Long

    suspend fun insertBatteryMetric(metric: BatteryMetric): Long

    suspend fun deleteCpuMetricsBefore(timestampMillis: Long)

    suspend fun deleteMemoryMetricsBefore(timestampMillis: Long)

    suspend fun deleteThermalMetricsBefore(timestampMillis: Long)

    suspend fun deleteBatteryMetricsBefore(timestampMillis: Long)
}
