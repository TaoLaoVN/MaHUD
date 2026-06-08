package com.cpumonitor.domain.repository

import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.analytics.AnalyticsDashboard
import com.cpumonitor.domain.model.analytics.DeviceHealthReport
import com.cpumonitor.domain.model.history.HistoryTimeRange
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for advanced analytics and device health scoring.
 */
interface AnalyticsRepository : Repository {

    fun observeAnalyticsDashboard(
        intervalMs: Long,
        window: HistoryTimeRange = HistoryTimeRange.TWENTY_FOUR_HOURS,
    ): Flow<Result<AnalyticsDashboard>>

    fun observeDeviceHealth(
        intervalMs: Long,
        window: HistoryTimeRange = HistoryTimeRange.TWENTY_FOUR_HOURS,
    ): Flow<Result<DeviceHealthReport>>

    suspend fun getAnalyticsDashboard(
        window: HistoryTimeRange = HistoryTimeRange.TWENTY_FOUR_HOURS,
    ): Result<AnalyticsDashboard>
}
