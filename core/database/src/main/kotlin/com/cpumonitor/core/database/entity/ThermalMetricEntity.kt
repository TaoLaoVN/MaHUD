package com.cpumonitor.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "thermal_metrics",
    indices = [Index(value = ["timestamp_millis"])],
)
data class ThermalMetricEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "timestamp_millis")
    val timestampMillis: Long,
    @ColumnInfo(name = "cpu_temperature_celsius")
    val cpuTemperatureCelsius: Float,
    @ColumnInfo(name = "battery_temperature_celsius")
    val batteryTemperatureCelsius: Float,
    @ColumnInfo(name = "thermal_zone_temperatures")
    val thermalZoneTemperatures: Map<String, Float>,
    @ColumnInfo(name = "is_throttling")
    val isThrottling: Boolean,
    @ColumnInfo(name = "is_overheating")
    val isOverheating: Boolean,
)
