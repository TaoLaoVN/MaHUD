package com.cpumonitor.domain.model.export

/**
 * Result of a successful metrics export operation.
 */
data class ExportedReport(
    val fileName: String,
    val absolutePath: String,
    val mimeType: String,
    val format: ExportFormat,
    val bytesWritten: Long,
)
