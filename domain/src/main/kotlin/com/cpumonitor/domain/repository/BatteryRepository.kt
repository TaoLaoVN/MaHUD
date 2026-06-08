package com.cpumonitor.domain.repository

import com.cpumonitor.domain.model.BatteryMetrics
import com.cpumonitor.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for battery monitoring data.
 * Feature modules must depend on this interface — never on [android.os.BatteryManager] directly.
 */
interface BatteryRepository : Repository {

    /**
     * Emits realtime battery status samples wrapped in [Result].
     *
     * @param intervalMs Polling interval in milliseconds. Clamped to [MonitoringConstants.MIN_REFRESH_INTERVAL_MS].
     */
    fun observeBatteryStatus(
        intervalMs: Long = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS,
    ): Flow<Result<BatteryMetrics>>
}
