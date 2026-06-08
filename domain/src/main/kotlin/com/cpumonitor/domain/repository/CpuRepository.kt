package com.cpumonitor.domain.repository

import com.cpumonitor.domain.model.CpuArchitectureInfo
import com.cpumonitor.domain.model.CpuUsageMetrics
import com.cpumonitor.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for CPU monitoring data.
 * Feature modules must depend on this interface — never on system APIs directly.
 */
interface CpuRepository : Repository {

    /**
     * Emits realtime CPU usage samples wrapped in [Result].
     *
     * @param intervalMs Polling interval in milliseconds. Clamped to [MonitoringConstants.MIN_REFRESH_INTERVAL_MS].
     */
    fun observeCpuUsage(
        intervalMs: Long = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS,
    ): Flow<Result<CpuUsageMetrics>>

    /**
     * Emits static CPU architecture information. Values are cached after the first successful read.
     */
    fun observeCpuArchitecture(): Flow<Result<CpuArchitectureInfo>>

    /**
     * Performs a one-shot read of CPU architecture information for persistence enrichment.
     */
    suspend fun getCpuArchitecture(): Result<CpuArchitectureInfo>
}
