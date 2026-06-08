package com.cpumonitor.domain.repository

import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.StorageMetrics
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for internal storage monitoring data.
 */
interface StorageRepository : Repository {

    /**
     * Emits realtime storage usage samples wrapped in [Result].
     */
    fun observeStorageUsage(
        intervalMs: Long = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS,
    ): Flow<Result<StorageMetrics>>

    /**
     * Performs a one-shot read of current storage metrics.
     */
    suspend fun getStorageUsage(): Result<StorageMetrics>
}
