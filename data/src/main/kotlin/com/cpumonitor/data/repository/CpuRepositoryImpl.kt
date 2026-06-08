package com.cpumonitor.data.repository

import com.cpumonitor.core.common.dispatcher.DispatchersProvider
import com.cpumonitor.core.monitoring.SystemMonitorProvider
import com.cpumonitor.domain.model.CpuArchitectureInfo
import com.cpumonitor.domain.model.CpuUsageMetrics
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.repository.CpuRepository
import com.cpumonitor.domain.repository.MonitoringConstants
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation for CPU monitoring metrics.
 */
@Singleton
class CpuRepositoryImpl @Inject constructor(
    private val systemMonitorProvider: SystemMonitorProvider,
    dispatchersProvider: DispatchersProvider,
) : BaseRepository(dispatchersProvider.io), CpuRepository {

    override fun observeCpuUsage(intervalMs: Long): Flow<Result<CpuUsageMetrics>> =
        observeSafely {
            systemMonitorProvider.observeCpuUsage(
                intervalMs = intervalMs.coerceAtLeast(MonitoringConstants.MIN_REFRESH_INTERVAL_MS),
            )
        }

    override fun observeCpuArchitecture(): Flow<Result<CpuArchitectureInfo>> =
        observeSafely(systemMonitorProvider::observeCpuArchitecture)

    override suspend fun getCpuArchitecture(): Result<CpuArchitectureInfo> =
        safeCall { systemMonitorProvider.getCpuArchitecture() }
}
