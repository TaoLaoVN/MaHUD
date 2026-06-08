package com.cpumonitor.domain.repository

import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.export.ExportFormat
import com.cpumonitor.domain.model.export.ExportedReport
import com.cpumonitor.domain.model.history.HistoryTimeRange

/**
 * Repository contract for exporting persisted metrics to user-requested report files.
 */
interface ExportRepository : Repository {

    /**
     * Generates a diagnostic report for the given [timeRange] and writes it to app-accessible storage.
     */
    suspend fun exportMetricsReport(
        format: ExportFormat,
        timeRange: HistoryTimeRange,
    ): Result<ExportedReport>
}
