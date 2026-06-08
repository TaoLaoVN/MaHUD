package com.cpumonitor.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "alert_history",
    indices = [Index(value = ["timestamp_millis"])],
)
data class AlertHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "rule_id")
    val ruleId: String,
    @ColumnInfo(name = "message")
    val message: String,
    @ColumnInfo(name = "metric_value")
    val metricValue: Float,
    @ColumnInfo(name = "timestamp_millis")
    val timestampMillis: Long,
)
