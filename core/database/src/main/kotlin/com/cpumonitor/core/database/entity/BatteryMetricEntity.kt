package com.cpumonitor.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "battery_metrics",
    indices = [Index(value = ["timestamp_millis"])],
)
data class BatteryMetricEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "timestamp_millis")
    val timestampMillis: Long,
    @ColumnInfo(name = "percentage")
    val percentage: Int,
    @ColumnInfo(name = "voltage_mv")
    val voltageMv: Int,
    @ColumnInfo(name = "current_ma")
    val currentMa: Int,
    @ColumnInfo(name = "capacity_mah")
    val capacityMah: Int,
    @ColumnInfo(name = "temperature_celsius")
    val temperatureCelsius: Float,
    @ColumnInfo(name = "health")
    val health: String,
    @ColumnInfo(name = "is_charging")
    val isCharging: Boolean,
    @ColumnInfo(name = "charge_speed_mw")
    val chargeSpeedMw: Int?,
)
