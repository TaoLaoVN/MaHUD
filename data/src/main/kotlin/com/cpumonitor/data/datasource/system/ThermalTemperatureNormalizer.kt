package com.cpumonitor.data.datasource.system

/**
 * Normalizes raw sysfs thermal readings to degrees Celsius.
 */
internal object ThermalTemperatureNormalizer {

    fun normalize(raw: Float): Float =
        if (raw > 1_000f) raw / 1_000f else raw
}
