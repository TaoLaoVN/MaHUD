package com.cpumonitor.data.datasource.overlay

import com.cpumonitor.core.common.dispatcher.DispatchersProvider
import com.cpumonitor.core.monitoring.FpsMonitorProvider
import com.cpumonitor.core.monitoring.SystemMonitorProvider
import com.cpumonitor.domain.model.OverlayMetrics
import com.cpumonitor.domain.repository.MonitoringConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Aggregates realtime CPU, RAM, thermal, and FPS streams into [OverlayMetrics].
 */
@Singleton
class SystemOverlayMetricsDataSource @Inject constructor(
    private val systemMonitorProvider: SystemMonitorProvider,
    private val fpsMonitorProvider: FpsMonitorProvider,
    private val dispatchersProvider: DispatchersProvider,
) {
    fun observe(refreshIntervalMs: Long): Flow<OverlayMetrics> {
        val intervalMs = refreshIntervalMs.coerceAtLeast(MonitoringConstants.MIN_REFRESH_INTERVAL_MS)

        return combine(
            systemMonitorProvider.observeCpuUsage(intervalMs),
            systemMonitorProvider.observeMemoryUsage(intervalMs),
            systemMonitorProvider.observeThermal(intervalMs),
            fpsMonitorProvider.observeFps(intervalMs),
        ) { cpu, memory, thermal, fps ->
            OverlayMetrics(
                cpuUsagePercent = cpu.totalUsagePercent,
                ramUsagePercent = toUsedPercent(memory),
                temperatureCelsius = thermal.primaryTemperatureCelsius,
                fps = fps.fps,
            )
        }.flowOn(dispatchersProvider.default)
    }

    private fun toUsedPercent(memory: com.cpumonitor.domain.model.MemoryMetrics): Float =
        if (memory.totalBytes <= 0L) {
            0f
        } else {
            ((memory.usedBytes.toDouble() / memory.totalBytes.toDouble()) * 100.0)
                .roundToInt()
                .toFloat()
        }
}
