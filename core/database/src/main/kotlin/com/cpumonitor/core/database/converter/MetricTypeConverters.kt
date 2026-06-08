package com.cpumonitor.core.database.converter

import androidx.room.TypeConverter

/**
 * Room type converters for complex metric column types.
 */
class MetricTypeConverters {

    @TypeConverter
    fun fromFloatList(value: List<Float>): String = value.joinToString(LIST_SEPARATOR)

    @TypeConverter
    fun toFloatList(value: String): List<Float> =
        if (value.isEmpty()) {
            emptyList()
        } else {
            value.split(LIST_SEPARATOR).map { it.toFloat() }
        }

    @TypeConverter
    fun fromZoneTemperatureMap(value: Map<String, Float>): String =
        value.entries.joinToString(MAP_ENTRY_SEPARATOR) { "${it.key}$MAP_KEY_VALUE_SEPARATOR${it.value}" }

    @TypeConverter
    fun toZoneTemperatureMap(value: String): Map<String, Float> =
        if (value.isEmpty()) {
            emptyMap()
        } else {
            value.split(MAP_ENTRY_SEPARATOR).associate { entry ->
                val parts = entry.split(MAP_KEY_VALUE_SEPARATOR, limit = 2)
                parts[0] to parts[1].toFloat()
            }
        }

    private companion object {
        const val LIST_SEPARATOR = ","
        const val MAP_ENTRY_SEPARATOR = "|"
        const val MAP_KEY_VALUE_SEPARATOR = "="
    }
}
