package com.cpumonitor.domain.repository

import com.cpumonitor.domain.model.OverlayMetrics
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for overlay permission checks and metric streams.
 */
interface OverlayRepository : Repository {
    fun observeMetrics(refreshIntervalMs: Long): Flow<OverlayMetrics>
    suspend fun isOverlayPermissionGranted(): Boolean
}
