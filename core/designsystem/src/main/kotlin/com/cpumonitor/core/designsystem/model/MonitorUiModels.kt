package com.cpumonitor.core.designsystem.model

import androidx.compose.ui.graphics.Color

/**
 * Immutable snapshot of a single metric value for UI rendering.
 */
data class MetricValueUiState(
    val label: String,
    val value: String,
    val unit: String = "",
    val accentColor: Color? = null,
)

/**
 * Immutable chart series data consumed by realtime chart composables.
 *
 * @param id stable identifier for chart state preservation across recompositions.
 * @param label human-readable series name shown in legends.
 * @param values ordered samples; newest value is appended at the end.
 * @param maxPoints rolling window size; older points are dropped when exceeded.
 * @param lineColor optional override; defaults to theme accent when null.
 */
data class ChartSeriesUiState(
    val id: String,
    val label: String,
    val values: List<Float>,
    val maxPoints: Int = 60,
    val lineColor: Color? = null,
) {
    init {
        require(maxPoints > 0) { "maxPoints must be positive" }
        require(values.size <= maxPoints) {
            "values size (${values.size}) must not exceed maxPoints ($maxPoints)"
        }
    }

    val latestValue: Float? get() = values.lastOrNull()
    val isEmpty: Boolean get() = values.isEmpty()
}

/**
 * Immutable widget state combining a headline metric and optional chart series.
 */
data class MonitorWidgetUiState(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val primaryMetric: MetricValueUiState,
    val chartSeries: ChartSeriesUiState? = null,
)

/**
 * Dashboard grid of monitoring widgets — fully immutable for Compose stability.
 */
data class MonitoringDashboardUiState(
    val widgets: List<MonitorWidgetUiState>,
    val lastUpdatedEpochMs: Long = 0L,
)
