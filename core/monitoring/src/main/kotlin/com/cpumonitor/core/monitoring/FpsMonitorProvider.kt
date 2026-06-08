package com.cpumonitor.core.monitoring

import com.cpumonitor.domain.model.FpsMetrics
import com.cpumonitor.domain.repository.MonitoringConstants
import kotlinx.coroutines.flow.Flow

/**
 * Native FPS monitoring abstraction. Implementations may use Choreographer, Display refresh rate,
 * or future GPU/frame-timeline integrations.
 */
interface FpsMonitorProvider {

    /**
     * Emits FPS samples at roughly [intervalMs] cadence.
     */
    fun observeFps(
        intervalMs: Long = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS,
    ): Flow<FpsMetrics>
}
