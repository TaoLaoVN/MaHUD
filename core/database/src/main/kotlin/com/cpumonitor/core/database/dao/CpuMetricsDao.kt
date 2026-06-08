package com.cpumonitor.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cpumonitor.core.database.entity.CpuMetricEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CpuMetricsDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: CpuMetricEntity): Long

    @Query(
        """
        SELECT * FROM cpu_metrics
        WHERE timestamp_millis >= :sinceMillis
        ORDER BY timestamp_millis ASC
        """,
    )
    fun observeSince(sinceMillis: Long): Flow<List<CpuMetricEntity>>

    @Query(
        """
        SELECT * FROM cpu_metrics
        WHERE timestamp_millis >= :sinceMillis
        ORDER BY timestamp_millis ASC
        """,
    )
    suspend fun getSince(sinceMillis: Long): List<CpuMetricEntity>

    @Query("DELETE FROM cpu_metrics WHERE timestamp_millis < :beforeMillis")
    suspend fun deleteBefore(beforeMillis: Long)
}
