package com.cpumonitor.core.monitoring

import com.cpumonitor.domain.model.BatteryMetrics
import com.cpumonitor.domain.model.CpuArchitectureInfo
import com.cpumonitor.domain.model.CpuUsageMetrics
import com.cpumonitor.domain.model.MemoryMetrics
import com.cpumonitor.domain.model.ThermalMetrics
import com.cpumonitor.domain.repository.MonitoringConstants
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over Android system monitoring APIs (`/proc`, `ActivityManager`, etc.).
 *
 * All system reads are centralized here. Feature modules must access monitoring data
 * exclusively through domain repositories backed by this provider.
 */
interface SystemMonitorProvider {

    /**
     * Emits realtime CPU usage derived from delta sampling of `/proc/stat`.
     *
     * @param intervalMs Sample interval. Values below [MonitoringConstants.MIN_REFRESH_INTERVAL_MS]
     *   are clamped to protect the 2% CPU overhead budget.
     */
    fun observeCpuUsage(
        intervalMs: Long = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS,
    ): Flow<CpuUsageMetrics>

    /**
     * Emits static CPU architecture information from `/proc/cpuinfo`.
     * The underlying read is cached after the first successful parse.
     */
    fun observeCpuArchitecture(): Flow<CpuArchitectureInfo>

    /**
     * Performs a one-shot read of CPU architecture information.
     */
    suspend fun getCpuArchitecture(): CpuArchitectureInfo

    /**
     * Emits realtime system memory metrics from [android.app.ActivityManager].
     *
     * @param intervalMs Sample interval. Values below [MonitoringConstants.MIN_REFRESH_INTERVAL_MS]
     *   are clamped to protect the 2% CPU overhead budget.
     */
    fun observeMemoryUsage(
        intervalMs: Long = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS,
    ): Flow<MemoryMetrics>

    /**
     * Emits realtime thermal readings from sysfs thermal zones and [android.os.BatteryManager].
     */
    fun observeThermal(
        intervalMs: Long = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS,
    ): Flow<ThermalMetrics>

    /**
     * Emits realtime battery status from [android.os.BatteryManager].
     */
    fun observeBattery(
        intervalMs: Long = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS,
    ): Flow<BatteryMetrics>
}
