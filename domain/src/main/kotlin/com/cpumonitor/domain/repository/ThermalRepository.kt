package com.cpumonitor.domain.repository

import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.ThermalMetrics
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for thermal monitoring data.
 * Feature modules must depend on this interface — never on sysfs APIs directly.
 */
interface ThermalRepository : Repository {

    /**
     * Emits realtime thermal readings wrapped in [Result].
     *
     * @param intervalMs Polling interval in milliseconds. Clamped to [MonitoringConstants.MIN_REFRESH_INTERVAL_MS].
     */
    fun observeThermal(
        intervalMs: Long = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS,
    ): Flow<Result<ThermalMetrics>>

    /**
     * Performs a one-shot read of all sysfs thermal zone temperatures.
     */
    suspend fun getThermalZones(): Result<Map<String, Float>>
}
