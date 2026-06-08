package com.cpumonitor.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cpu_metrics",
    indices = [Index(value = ["timestamp_millis"])],
)
data class CpuMetricEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "timestamp_millis")
    val timestampMillis: Long,
    @ColumnInfo(name = "total_usage_percent")
    val totalUsagePercent: Float,
    @ColumnInfo(name = "per_core_usage_percent")
    val perCoreUsagePercent: List<Float>,
    @ColumnInfo(name = "big_core_usage_percent")
    val bigCoreUsagePercent: Float,
    @ColumnInfo(name = "little_core_usage_percent")
    val littleCoreUsagePercent: Float,
    @ColumnInfo(name = "current_frequency_mhz")
    val currentFrequencyMhz: Float,
    @ColumnInfo(name = "min_frequency_mhz")
    val minFrequencyMhz: Float,
    @ColumnInfo(name = "max_frequency_mhz")
    val maxFrequencyMhz: Float,
)
