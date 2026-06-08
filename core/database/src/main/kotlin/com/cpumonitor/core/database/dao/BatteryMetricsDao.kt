package com.cpumonitor.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cpumonitor.core.database.entity.BatteryMetricEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryMetricsDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: BatteryMetricEntity): Long

    @Query(
        """
        SELECT * FROM battery_metrics
        WHERE timestamp_millis >= :sinceMillis
        ORDER BY timestamp_millis ASC
        """,
    )
    fun observeSince(sinceMillis: Long): Flow<List<BatteryMetricEntity>>

    @Query(
        """
        SELECT * FROM battery_metrics
        WHERE timestamp_millis >= :sinceMillis
        ORDER BY timestamp_millis ASC
        """,
    )
    suspend fun getSince(sinceMillis: Long): List<BatteryMetricEntity>

    @Query("DELETE FROM battery_metrics WHERE timestamp_millis < :beforeMillis")
    suspend fun deleteBefore(beforeMillis: Long)
}
