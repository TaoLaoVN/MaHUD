package com.cpumonitor.domain.model.export

/**
 * Supported export formats for diagnostic reports.
 */
enum class ExportFormat(val displayName: String, val mimeType: String, val fileExtension: String) {
    CSV("CSV", "text/csv", "csv"),
    JSON("JSON", "application/json", "json"),
    PDF("PDF", "application/pdf", "pdf"),
}
