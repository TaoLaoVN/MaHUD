package com.cpumonitor.domain.repository

import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.metric.BatteryMetric
import com.cpumonitor.domain.model.metric.CpuMetric
import com.cpumonitor.domain.model.metric.MemoryMetric
import com.cpumonitor.domain.model.metric.ThermalMetric
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for persisting and querying historical system metrics.
 * Feature modules must depend on this interface only.
 */
interface MetricsRepository : Repository {

    fun observeCpuMetrics(sinceMillis: Long): Flow<List<CpuMetric>>

    fun observeMemoryMetrics(sinceMillis: Long): Flow<List<MemoryMetric>>

    fun observeThermalMetrics(sinceMillis: Long): Flow<List<ThermalMetric>>

    fun observeBatteryMetrics(sinceMillis: Long): Flow<List<BatteryMetric>>

    suspend fun getCpuMetrics(sinceMillis: Long): Result<List<CpuMetric>>

    suspend fun getMemoryMetrics(sinceMillis: Long): Result<List<MemoryMetric>>

    suspend fun getThermalMetrics(sinceMillis: Long): Result<List<ThermalMetric>>

    suspend fun getBatteryMetrics(sinceMillis: Long): Result<List<BatteryMetric>>

    suspend fun insertCpuMetric(metric: CpuMetric): Result<Long>

    suspend fun insertMemoryMetric(metric: MemoryMetric): Result<Long>

    suspend fun insertThermalMetric(metric: ThermalMetric): Result<Long>

    suspend fun insertBatteryMetric(metric: BatteryMetric): Result<Long>

    suspend fun deleteCpuMetricsBefore(timestampMillis: Long): Result<Unit>

    suspend fun deleteMemoryMetricsBefore(timestampMillis: Long): Result<Unit>

    suspend fun deleteThermalMetricsBefore(timestampMillis: Long): Result<Unit>

    suspend fun deleteBatteryMetricsBefore(timestampMillis: Long): Result<Unit>
}
