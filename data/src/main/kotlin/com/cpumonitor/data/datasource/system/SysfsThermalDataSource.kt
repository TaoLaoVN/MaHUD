package com.cpumonitor.data.datasource.system

import com.cpumonitor.data.datasource.SystemDataSource
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads CPU/SOC temperature from Linux sysfs thermal zones under `/sys/class/thermal`.
 */
@Singleton
class SysfsThermalDataSource @Inject constructor() : SystemDataSource {

    fun readCpuTemperatureCelsius(): Float {
        val zones = readThermalZones()
        if (zones.isEmpty()) return 0f

        val preferred = zones.filterKeys { key ->
            CPU_ZONE_HINTS.any { hint -> key.contains(hint, ignoreCase = true) }
        }
        val candidates = if (preferred.isNotEmpty()) preferred.values else zones.values
        return candidates.maxOrNull() ?: 0f
    }

    fun readThermalZones(): Map<String, Float> {
        val thermalRoot = File(THERMAL_CLASS_PATH)
        if (!thermalRoot.isDirectory) return emptyMap()

        return thermalRoot.listFiles()
            .orEmpty()
            .filter { it.isDirectory && it.name.startsWith("thermal_zone") }
            .mapNotNull { zoneDir ->
                val type = readZoneType(zoneDir) ?: zoneDir.name
                val temperature = readZoneTemperature(zoneDir) ?: return@mapNotNull null
                type to temperature
            }
            .toMap()
    }

    private fun readZoneType(zoneDir: File): String? =
        runCatching { File(zoneDir, "type").readText().trim() }
            .getOrNull()
            ?.takeIf { it.isNotEmpty() }

    private fun readZoneTemperature(zoneDir: File): Float? {
        val raw = runCatching { File(zoneDir, "temp").readText().trim().toFloat() }
            .getOrNull() ?: return null
        return normalizeTemperature(raw)
    }

    private fun normalizeTemperature(raw: Float): Float =
        ThermalTemperatureNormalizer.normalize(raw)

    companion object {
        private const val THERMAL_CLASS_PATH = "/sys/class/thermal"

        private val CPU_ZONE_HINTS = listOf(
            "cpu",
            "soc",
            "tsens",
            "ap",
            "cluster",
            "big",
            "little",
            "pack",
        )
    }
}
