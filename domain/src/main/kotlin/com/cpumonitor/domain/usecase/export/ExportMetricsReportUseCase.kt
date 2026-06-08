package com.cpumonitor.domain.usecase.export

import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.export.ExportFormat
import com.cpumonitor.domain.model.export.ExportedReport
import com.cpumonitor.domain.model.history.HistoryTimeRange
import com.cpumonitor.domain.repository.ExportRepository
import com.cpumonitor.domain.usecase.UseCase
import javax.inject.Inject

data class ExportMetricsReportParams(
    val format: ExportFormat,
    val timeRange: HistoryTimeRange,
)

/**
 * Generates an on-demand diagnostic export for the selected format and time window.
 */
class ExportMetricsReportUseCase @Inject constructor(
    private val exportRepository: ExportRepository,
) : UseCase<ExportMetricsReportParams, ExportedReport>() {

    override suspend fun execute(params: ExportMetricsReportParams): Result<ExportedReport> =
        exportRepository.exportMetricsReport(
            format = params.format,
            timeRange = params.timeRange,
        )
}
