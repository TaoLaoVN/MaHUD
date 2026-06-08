package com.cpumonitor.data.datasource.system

import com.cpumonitor.data.datasource.SystemDataSource
import com.cpumonitor.domain.model.device.CpuCoreSysfsInfo
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads per-core CPU frequency and governor data from sysfs cpufreq nodes.
 */
@Singleton
class CpuSysfsDataSource @Inject constructor() : SystemDataSource {

    fun readCpuCores(): List<CpuCoreSysfsInfo> {
        val coreCount = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        return (0 until coreCount).mapNotNull(::readCore).ifEmpty {
            discoverCpuDirectories().mapNotNull { dir ->
                val index = dir.name.removePrefix("cpu").toIntOrNull() ?: return@mapNotNull null
                readCore(index)
            }
        }
    }

    private fun discoverCpuDirectories(): List<File> {
        val cpuRoot = File(CPU_ROOT)
        if (!cpuRoot.isDirectory) return emptyList()
        return cpuRoot.listFiles()
            .orEmpty()
            .filter { file ->
                file.isDirectory && file.name.matches(Regex("cpu\\d+"))
            }
            .sortedBy { it.name.removePrefix("cpu").toIntOrNull() ?: Int.MAX_VALUE }
    }

    private fun readCore(index: Int): CpuCoreSysfsInfo? {
        val cpufreqDir = File("$CPU_ROOT/cpu$index/cpufreq")
        if (!cpufreqDir.isDirectory) return null

        return CpuCoreSysfsInfo(
            index = index,
            currentFrequencyKhz = readIntFile(File(cpufreqDir, "scaling_cur_freq"))
                ?: readIntFile(File(cpufreqDir, "cpuinfo_cur_freq")),
            minFrequencyKhz = readIntFile(File(cpufreqDir, "scaling_min_freq"))
                ?: readIntFile(File(cpufreqDir, "cpuinfo_min_freq")),
            maxFrequencyKhz = readIntFile(File(cpufreqDir, "scaling_max_freq"))
                ?: readIntFile(File(cpufreqDir, "cpuinfo_max_freq")),
            governor = readTextFile(File(cpufreqDir, "scaling_governor")),
        )
    }

    private fun readIntFile(file: File): Int? {
        if (!file.canRead()) return null
        return file.readText().trim().toIntOrNull()
    }

    private fun readTextFile(file: File): String? {
        if (!file.canRead()) return null
        return file.readText().trim().takeIf { it.isNotEmpty() }
    }

    companion object {
        private const val CPU_ROOT = "/sys/devices/system/cpu"
    }
}
