package com.cpumonitor.data.repository

import com.cpumonitor.core.common.dispatcher.DispatchersProvider
import com.cpumonitor.core.monitoring.SystemMonitorProvider
import com.cpumonitor.domain.model.MemoryMetrics
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.repository.MemoryRepository
import com.cpumonitor.domain.repository.MonitoringConstants
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation for system memory monitoring metrics.
 */
@Singleton
class MemoryRepositoryImpl @Inject constructor(
    private val systemMonitorProvider: SystemMonitorProvider,
    dispatchersProvider: DispatchersProvider,
) : BaseRepository(dispatchersProvider.io), MemoryRepository {

    override fun observeMemoryUsage(intervalMs: Long): Flow<Result<MemoryMetrics>> =
        observeSafely {
            systemMonitorProvider.observeMemoryUsage(
                intervalMs = intervalMs.coerceAtLeast(MonitoringConstants.MIN_REFRESH_INTERVAL_MS),
            )
        }
}
