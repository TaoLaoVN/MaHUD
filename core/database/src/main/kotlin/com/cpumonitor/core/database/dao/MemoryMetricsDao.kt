package com.cpumonitor.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cpumonitor.core.database.entity.MemoryMetricEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryMetricsDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: MemoryMetricEntity): Long

    @Query(
        """
        SELECT * FROM memory_metrics
        WHERE timestamp_millis >= :sinceMillis
        ORDER BY timestamp_millis ASC
        """,
    )
    fun observeSince(sinceMillis: Long): Flow<List<MemoryMetricEntity>>

    @Query(
        """
        SELECT * FROM memory_metrics
        WHERE timestamp_millis >= :sinceMillis
        ORDER BY timestamp_millis ASC
        """,
    )
    suspend fun getSince(sinceMillis: Long): List<MemoryMetricEntity>

    @Query("DELETE FROM memory_metrics WHERE timestamp_millis < :beforeMillis")
    suspend fun deleteBefore(beforeMillis: Long)
}
