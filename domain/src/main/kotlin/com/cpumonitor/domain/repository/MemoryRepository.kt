package com.cpumonitor.domain.repository

import com.cpumonitor.domain.model.MemoryMetrics
import com.cpumonitor.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for system memory monitoring data.
 * Feature modules must depend on this interface — never on system APIs directly.
 */
interface MemoryRepository : Repository {

    /**
     * Emits realtime memory usage samples wrapped in [Result].
     *
     * @param intervalMs Polling interval in milliseconds. Clamped to [MonitoringConstants.MIN_REFRESH_INTERVAL_MS].
     */
    fun observeMemoryUsage(
        intervalMs: Long = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS,
    ): Flow<Result<MemoryMetrics>>
}
