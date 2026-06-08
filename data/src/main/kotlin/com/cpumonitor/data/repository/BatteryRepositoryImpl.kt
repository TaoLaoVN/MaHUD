package com.cpumonitor.data.repository

import com.cpumonitor.core.common.dispatcher.DispatchersProvider
import com.cpumonitor.core.monitoring.SystemMonitorProvider
import com.cpumonitor.domain.model.BatteryMetrics
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.repository.BatteryRepository
import com.cpumonitor.domain.repository.MonitoringConstants
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation for battery monitoring metrics.
 */
@Singleton
class BatteryRepositoryImpl @Inject constructor(
    private val systemMonitorProvider: SystemMonitorProvider,
    dispatchersProvider: DispatchersProvider,
) : BaseRepository(dispatchersProvider.io), BatteryRepository {

    override fun observeBatteryStatus(intervalMs: Long): Flow<Result<BatteryMetrics>> =
        observeSafely {
            systemMonitorProvider.observeBattery(
                intervalMs = intervalMs.coerceAtLeast(MonitoringConstants.MIN_REFRESH_INTERVAL_MS),
            )
        }
}
