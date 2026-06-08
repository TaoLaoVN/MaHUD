package com.cpumonitor.core.ui.localized

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.cpumonitor.core.ui.R
import com.cpumonitor.domain.model.StorageCategory
import com.cpumonitor.domain.model.alert.AlertComparator
import com.cpumonitor.domain.model.alert.AlertMetricType
import com.cpumonitor.domain.model.analytics.HealthStatus
import com.cpumonitor.domain.model.benchmark.BenchmarkMode
import com.cpumonitor.domain.model.benchmark.StressTestDuration
import com.cpumonitor.domain.model.export.ExportFormat
import com.cpumonitor.domain.model.history.HistoryMetricType
import com.cpumonitor.domain.model.history.HistoryTimeRange
import com.cpumonitor.domain.model.process.ProcessSortOrder
import com.cpumonitor.domain.model.settings.AppTheme

@Composable
fun HealthStatus.localizedName(): String = when (this) {
    HealthStatus.EXCELLENT -> stringResource(R.string.health_excellent)
    HealthStatus.GOOD -> stringResource(R.string.health_good)
    HealthStatus.FAIR -> stringResource(R.string.health_fair)
    HealthStatus.POOR -> stringResource(R.string.health_poor)
    HealthStatus.CRITICAL -> stringResource(R.string.health_critical)
}

@Composable
fun HistoryMetricType.localizedName(): String = when (this) {
    HistoryMetricType.CPU -> stringResource(R.string.metric_cpu)
    HistoryMetricType.MEMORY -> stringResource(R.string.metric_memory)
    HistoryMetricType.THERMAL -> stringResource(R.string.metric_thermal)
    HistoryMetricType.BATTERY -> stringResource(R.string.metric_battery)
}

@Composable
fun HistoryTimeRange.localizedName(): String = when (this) {
    HistoryTimeRange.ONE_MINUTE -> stringResource(R.string.range_1m)
    HistoryTimeRange.FIVE_MINUTES -> stringResource(R.string.range_5m)
    HistoryTimeRange.FIFTEEN_MINUTES -> stringResource(R.string.range_15m)
    HistoryTimeRange.ONE_HOUR -> stringResource(R.string.range_1h)
    HistoryTimeRange.TWENTY_FOUR_HOURS -> stringResource(R.string.range_24h)
}

@Composable
fun AppTheme.localizedName(): String = when (this) {
    AppTheme.LIGHT -> stringResource(R.string.theme_light)
    AppTheme.DARK -> stringResource(R.string.theme_dark)
    AppTheme.AMOLED -> stringResource(R.string.theme_amoled)
}

@Composable
fun ProcessSortOrder.localizedName(): String = when (this) {
    ProcessSortOrder.CPU -> stringResource(R.string.sort_cpu)
    ProcessSortOrder.MEMORY -> stringResource(R.string.sort_memory)
    ProcessSortOrder.NAME -> stringResource(R.string.sort_name)
}

@Composable
fun BenchmarkMode.localizedName(): String = when (this) {
    BenchmarkMode.SINGLE_CORE -> stringResource(R.string.benchmark_mode_single)
    BenchmarkMode.MULTI_CORE -> stringResource(R.string.benchmark_mode_multi)
}

@Composable
fun StressTestDuration.localizedName(): String = when (this) {
    StressTestDuration.MINUTES_5 -> stringResource(R.string.benchmark_stress_5m)
    StressTestDuration.MINUTES_10 -> stringResource(R.string.benchmark_stress_10m)
    StressTestDuration.MINUTES_30 -> stringResource(R.string.benchmark_stress_30m)
}

@Composable
fun StorageCategory.localizedName(): String = when (this) {
    StorageCategory.APPLICATIONS -> stringResource(R.string.storage_cat_apps)
    StorageCategory.IMAGES -> stringResource(R.string.storage_cat_images)
    StorageCategory.VIDEOS -> stringResource(R.string.storage_cat_videos)
    StorageCategory.AUDIO -> stringResource(R.string.storage_cat_audio)
    StorageCategory.DOCUMENTS -> stringResource(R.string.storage_cat_documents)
    StorageCategory.DOWNLOADS -> stringResource(R.string.storage_cat_downloads)
    StorageCategory.OTHER -> stringResource(R.string.storage_cat_other)
}

@Composable
fun ExportFormat.localizedName(): String = when (this) {
    ExportFormat.CSV -> "CSV"
    ExportFormat.JSON -> "JSON"
    ExportFormat.PDF -> "PDF"
}

@Composable
fun AlertMetricType.localizedName(): String = when (this) {
    AlertMetricType.CPU -> stringResource(R.string.metric_cpu)
    AlertMetricType.MEMORY -> stringResource(R.string.metric_memory)
    AlertMetricType.THERMAL -> stringResource(R.string.metric_thermal)
    AlertMetricType.BATTERY -> stringResource(R.string.metric_battery)
}

@Composable
fun AlertComparator.localizedSymbol(): String = when (this) {
    AlertComparator.GREATER_THAN -> stringResource(R.string.alert_comparator_gt)
    AlertComparator.LESS_THAN -> stringResource(R.string.alert_comparator_lt)
}
