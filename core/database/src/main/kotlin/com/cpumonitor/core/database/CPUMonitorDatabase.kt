package com.cpumonitor.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cpumonitor.core.database.converter.MetricTypeConverters
import com.cpumonitor.core.database.dao.AlertHistoryDao
import com.cpumonitor.core.database.dao.BatteryMetricsDao
import com.cpumonitor.core.database.dao.CpuMetricsDao
import com.cpumonitor.core.database.dao.MemoryMetricsDao
import com.cpumonitor.core.database.dao.ThermalMetricsDao
import com.cpumonitor.core.database.entity.AlertHistoryEntity
import com.cpumonitor.core.database.entity.BatteryMetricEntity
import com.cpumonitor.core.database.entity.CpuMetricEntity
import com.cpumonitor.core.database.entity.MemoryMetricEntity
import com.cpumonitor.core.database.entity.ThermalMetricEntity

/**
 * Room database for historical system metrics.
 */
@Database(
    entities = [
        CpuMetricEntity::class,
        MemoryMetricEntity::class,
        ThermalMetricEntity::class,
        BatteryMetricEntity::class,
        AlertHistoryEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(MetricTypeConverters::class)
abstract class CPUMonitorDatabase : RoomDatabase() {

    abstract fun cpuMetricsDao(): CpuMetricsDao

    abstract fun memoryMetricsDao(): MemoryMetricsDao

    abstract fun thermalMetricsDao(): ThermalMetricsDao

    abstract fun batteryMetricsDao(): BatteryMetricsDao

    abstract fun alertHistoryDao(): AlertHistoryDao
}
