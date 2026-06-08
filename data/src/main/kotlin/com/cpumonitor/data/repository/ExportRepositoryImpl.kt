package com.cpumonitor.data.repository

import android.content.Context
import com.cpumonitor.core.common.dispatcher.DispatchersProvider
import com.cpumonitor.data.datasource.export.MetricsReportBuilder
import com.cpumonitor.data.datasource.export.MetricsReportSnapshot
import com.cpumonitor.data.datasource.export.PdfReportWriter
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.export.ExportFormat
import com.cpumonitor.domain.model.export.ExportedReport
import com.cpumonitor.domain.model.history.HistoryTimeRange
import com.cpumonitor.domain.repository.ExportRepository
import com.cpumonitor.domain.repository.MetricsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val metricsRepository: MetricsRepository,
    dispatchersProvider: DispatchersProvider,
) : BaseRepository(dispatchersProvider.io), ExportRepository {

    override suspend fun exportMetricsReport(
        format: ExportFormat,
        timeRange: HistoryTimeRange,
    ): Result<ExportedReport> = safeCall {
        val sinceMillis = System.currentTimeMillis() - timeRange.durationMillis
        val snapshot = loadSnapshot(sinceMillis)
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = FILE_TIMESTAMP_FORMAT.format(Date())
        val fileName = "cpu_monitor_report_${timestamp}.${format.fileExtension}"
        val outputFile = File(exportDir, fileName)

        when (format) {
            ExportFormat.CSV -> outputFile.writeText(MetricsReportBuilder.buildCsv(snapshot))
            ExportFormat.JSON -> outputFile.writeText(MetricsReportBuilder.buildJson(snapshot))
            ExportFormat.PDF -> PdfReportWriter.write(
                file = outputFile,
                lines = MetricsReportBuilder.buildPdfLines(snapshot),
            )
        }

        ExportedReport(
            fileName = fileName,
            absolutePath = outputFile.absolutePath,
            mimeType = format.mimeType,
            format = format,
            bytesWritten = outputFile.length(),
        )
    }

    private suspend fun loadSnapshot(sinceMillis: Long): MetricsReportSnapshot {
        val cpu = unwrap(metricsRepository.getCpuMetrics(sinceMillis))
        val memory = unwrap(metricsRepository.getMemoryMetrics(sinceMillis))
        val thermal = unwrap(metricsRepository.getThermalMetrics(sinceMillis))
        val battery = unwrap(metricsRepository.getBatteryMetrics(sinceMillis))
        return MetricsReportSnapshot(
            cpuMetrics = cpu,
            memoryMetrics = memory,
            thermalMetrics = thermal,
            batteryMetrics = battery,
            sinceMillis = sinceMillis,
            generatedAtMillis = System.currentTimeMillis(),
        )
    }

    private fun <T> unwrap(result: Result<T>): T =
        when (result) {
            is Result.Success -> result.data
            is Result.Error -> throw result.exception
            Result.Loading -> error("Unexpected loading state")
        }

    private companion object {
        val FILE_TIMESTAMP_FORMAT = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    }
}
