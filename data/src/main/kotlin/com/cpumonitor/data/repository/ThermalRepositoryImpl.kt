package com.cpumonitor.data.repository

import com.cpumonitor.core.common.dispatcher.DispatchersProvider
import com.cpumonitor.core.monitoring.SystemMonitorProvider
import com.cpumonitor.data.datasource.system.SysfsThermalDataSource
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.ThermalMetrics
import com.cpumonitor.domain.repository.MonitoringConstants
import com.cpumonitor.domain.repository.ThermalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation for thermal monitoring metrics.
 */
@Singleton
class ThermalRepositoryImpl @Inject constructor(
    private val systemMonitorProvider: SystemMonitorProvider,
    private val sysfsThermalDataSource: SysfsThermalDataSource,
    dispatchersProvider: DispatchersProvider,
) : BaseRepository(dispatchersProvider.io), ThermalRepository {

    override fun observeThermal(intervalMs: Long): Flow<Result<ThermalMetrics>> =
        observeSafely {
            systemMonitorProvider.observeThermal(
                intervalMs = intervalMs.coerceAtLeast(MonitoringConstants.MIN_REFRESH_INTERVAL_MS),
            )
        }

    override suspend fun getThermalZones(): Result<Map<String, Float>> =
        safeCall { sysfsThermalDataSource.readThermalZones() }
}
