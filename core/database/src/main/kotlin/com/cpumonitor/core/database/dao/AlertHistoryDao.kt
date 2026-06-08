package com.cpumonitor.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cpumonitor.core.database.entity.AlertHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertHistoryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: AlertHistoryEntity): Long

    @Query(
        """
        SELECT * FROM alert_history
        ORDER BY timestamp_millis DESC
        LIMIT :limit
        """,
    )
    fun observeRecent(limit: Int): Flow<List<AlertHistoryEntity>>

    @Query("DELETE FROM alert_history WHERE timestamp_millis < :beforeMillis")
    suspend fun deleteBefore(beforeMillis: Long)
}
