package com.cpumonitor.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "memory_metrics",
    indices = [Index(value = ["timestamp_millis"])],
)
data class MemoryMetricEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "timestamp_millis")
    val timestampMillis: Long,
    @ColumnInfo(name = "used_bytes")
    val usedBytes: Long,
    @ColumnInfo(name = "available_bytes")
    val availableBytes: Long,
    @ColumnInfo(name = "free_bytes")
    val freeBytes: Long,
    @ColumnInfo(name = "cached_bytes")
    val cachedBytes: Long,
)
