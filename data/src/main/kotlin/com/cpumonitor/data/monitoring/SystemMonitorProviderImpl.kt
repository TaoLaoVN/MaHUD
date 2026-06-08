package com.cpumonitor.data.monitoring

import com.cpumonitor.core.monitoring.SystemMonitorProvider
import com.cpumonitor.data.datasource.proc.ProcCpuInfoDataSource
import com.cpumonitor.data.datasource.proc.ProcStatDataSource
import com.cpumonitor.data.datasource.proc.ProcStatParser
import com.cpumonitor.data.datasource.proc.ProcStatSnapshot
import com.cpumonitor.data.datasource.system.ActivityManagerMemoryDataSource
import com.cpumonitor.data.datasource.system.BatteryManagerDataSource
import com.cpumonitor.data.datasource.system.BatteryTemperatureDataSource
import com.cpumonitor.data.datasource.system.SysfsThermalDataSource
import com.cpumonitor.data.util.distinctUntilChangedBy
import com.cpumonitor.data.util.monitoringDeltaFlow
import com.cpumonitor.data.util.monitoringPollFlow
import com.cpumonitor.domain.model.BatteryMetrics
import com.cpumonitor.domain.model.CpuArchitectureInfo
import com.cpumonitor.domain.model.CpuUsageMetrics
import com.cpumonitor.domain.model.MemoryMetrics
import com.cpumonitor.domain.model.ThermalMetrics
import com.cpumonitor.domain.repository.MonitoringConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default [SystemMonitorProvider] wiring proc-based CPU reads and ActivityManager memory reads.
 */
@Singleton
class SystemMonitorProviderImpl @Inject constructor(
    private val procStatDataSource: ProcStatDataSource,
    private val procCpuInfoDataSource: ProcCpuInfoDataSource,
    private val activityManagerMemoryDataSource: ActivityManagerMemoryDataSource,
    private val sysfsThermalDataSource: SysfsThermalDataSource,
    private val batteryTemperatureDataSource: BatteryTemperatureDataSource,
    private val batteryManagerDataSource: BatteryManagerDataSource,
) : SystemMonitorProvider {

    override fun observeCpuUsage(intervalMs: Long): Flow<CpuUsageMetrics> =
        monitoringDeltaFlow(
            intervalMs = intervalMs,
            sample = procStatDataSource::readSnapshot,
            transform = ::toCpuUsageMetrics,
        )
            .distinctUntilChangedBy { metrics ->
                metrics.totalUsagePercent to metrics.perCoreUsagePercent
            }
            .flowOn(Dispatchers.IO)

    override fun observeCpuArchitecture(): Flow<CpuArchitectureInfo> = flow {
        emit(getCpuArchitecture())
    }.flowOn(Dispatchers.IO)

    override suspend fun getCpuArchitecture(): CpuArchitectureInfo =
        procCpuInfoDataSource.readArchitecture()

    override fun observeMemoryUsage(intervalMs: Long): Flow<MemoryMetrics> =
        monitoringPollFlow(intervalMs) {
            activityManagerMemoryDataSource.readMemoryMetrics()
        }
            .distinctUntilChangedBy { metrics ->
                metrics.usedBytes to metrics.availableBytes
            }
            .flowOn(Dispatchers.IO)

    override fun observeThermal(intervalMs: Long): Flow<ThermalMetrics> =
        monitoringPollFlow(intervalMs) {
            ThermalMetrics(
                timestampMillis = System.currentTimeMillis(),
                cpuTemperatureCelsius = sysfsThermalDataSource.readCpuTemperatureCelsius(),
                batteryTemperatureCelsius = batteryTemperatureDataSource.readBatteryTemperatureCelsius(),
            )
        }
            .distinctUntilChangedBy { metrics ->
                metrics.cpuTemperatureCelsius to metrics.batteryTemperatureCelsius
            }
            .flowOn(Dispatchers.IO)

    override fun observeBattery(intervalMs: Long): Flow<BatteryMetrics> =
        monitoringPollFlow(intervalMs) {
            batteryManagerDataSource.readBatteryMetrics()
        }
            .distinctUntilChangedBy { metrics ->
                metrics.percentage to metrics.isCharging
            }
            .flowOn(Dispatchers.IO)

    private fun toCpuUsageMetrics(
        previous: ProcStatSnapshot,
        current: ProcStatSnapshot,
    ): CpuUsageMetrics {
        val perCoreUsage = List(current.perCore.size) { index ->
            val prevCore = previous.perCore.getOrNull(index)
            val currCore = current.perCore.getOrNull(index)
            if (prevCore != null && currCore != null) {
                ProcStatParser.calculateUsagePercent(prevCore, currCore)
            } else {
                0f
            }
        }

        return CpuUsageMetrics(
            timestampMillis = current.timestampMillis,
            totalUsagePercent = ProcStatParser.calculateUsagePercent(
                previous.aggregate,
                current.aggregate,
            ),
            perCoreUsagePercent = perCoreUsage,
        )
    }

    companion object {
        val DEFAULT_INTERVAL_MS: Long = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS
    }
}
