package com.cpumonitor.data.repository

import com.cpumonitor.core.common.dispatcher.DispatchersProvider
import com.cpumonitor.data.datasource.proc.ProcCpuInfoDataSource
import com.cpumonitor.data.datasource.proc.ProcStatDataSource
import com.cpumonitor.data.datasource.system.ActivityManagerMemoryDataSource
import com.cpumonitor.data.datasource.system.BuildInfoDataSource
import com.cpumonitor.data.datasource.system.CpuSysfsDataSource
import com.cpumonitor.data.datasource.system.DevicePeripheralsDataSource
import com.cpumonitor.data.datasource.system.DisplayInfoDataSource
import com.cpumonitor.data.datasource.system.GpuInfoDataSource
import com.cpumonitor.data.datasource.system.StorageDataSource
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.device.AuthenticityReport
import com.cpumonitor.domain.model.device.DeviceSpec
import com.cpumonitor.domain.repository.DeviceRepository
import com.cpumonitor.domain.validation.DeviceAuthenticityValidator
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepositoryImpl @Inject constructor(
    private val buildInfoDataSource: BuildInfoDataSource,
    private val procCpuInfoDataSource: ProcCpuInfoDataSource,
    private val cpuSysfsDataSource: CpuSysfsDataSource,
    private val gpuInfoDataSource: GpuInfoDataSource,
    private val displayInfoDataSource: DisplayInfoDataSource,
    private val devicePeripheralsDataSource: DevicePeripheralsDataSource,
    private val activityManagerMemoryDataSource: ActivityManagerMemoryDataSource,
    private val storageDataSource: StorageDataSource,
    dispatchersProvider: DispatchersProvider,
) : BaseRepository(dispatchersProvider.io), DeviceRepository {

    override suspend fun getDeviceSpec(): Result<DeviceSpec> = safeCall {
        val memory = activityManagerMemoryDataSource.readMemoryMetrics()
        val storage = storageDataSource.readStorageMetrics()

        DeviceSpec(
            timestampMillis = System.currentTimeMillis(),
            buildInfo = buildInfoDataSource.readBuildInfo(),
            softwareInfo = buildInfoDataSource.readSoftwareInfo(),
            cpuArchitecture = procCpuInfoDataSource.readArchitecture(),
            cpuCores = cpuSysfsDataSource.readCpuCores(),
            gpuInfo = gpuInfoDataSource.readGpuInfo(),
            screenInfo = displayInfoDataSource.readScreenInfo(),
            peripherals = devicePeripheralsDataSource.readPeripheralsInfo(),
            totalRamBytes = memory.totalBytes,
            totalStorageBytes = storage.totalBytes,
            cpuUsageMonitoringAvailable = isCpuUsageMonitoringAvailable(),
        )
    }

    override suspend fun validateAuthenticity(): Result<AuthenticityReport> = safeCall {
        when (val specResult = getDeviceSpec()) {
            is Result.Success -> DeviceAuthenticityValidator.validate(specResult.data)
            is Result.Error -> throw specResult.exception
            Result.Loading -> error("Device spec is still loading")
        }
    }

    private fun isCpuUsageMonitoringAvailable(): Boolean {
        return try {
            val file = File(ProcStatDataSource.PROC_STAT_PATH)
            file.canRead() && file.readText().lineSequence().any { it.startsWith("cpu ") }
        } catch (_: Exception) {
            false
        }
    }
}
