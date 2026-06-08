package com.cpumonitor.data.datasource.proc

import android.os.Build
import com.cpumonitor.data.datasource.SystemDataSource
import com.cpumonitor.domain.model.CpuArchitectureInfo
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads and parses `/proc/cpuinfo` for static CPU architecture metadata.
 * Results are cached in-memory because cpuinfo content is effectively immutable at runtime.
 */
@Singleton
class ProcCpuInfoDataSource @Inject constructor() : SystemDataSource {

    private val cpuInfoFile = File(PROC_CPUINFO_PATH)

    @Volatile
    private var cachedArchitecture: CpuArchitectureInfo? = null

    fun readArchitecture(): CpuArchitectureInfo {
        cachedArchitecture?.let { return it }

        synchronized(this) {
            cachedArchitecture?.let { return it }
            val parsed = ProcCpuInfoParser.parse(
                content = cpuInfoFile.readText(),
                abi = resolvePrimaryAbi(),
            )
            cachedArchitecture = parsed
            return parsed
        }
    }

    companion object {
        const val PROC_CPUINFO_PATH = "/proc/cpuinfo"

        private fun resolvePrimaryAbi(): String =
            Build.SUPPORTED_ABIS.firstOrNull() ?: Build.CPU_ABI
    }
}
