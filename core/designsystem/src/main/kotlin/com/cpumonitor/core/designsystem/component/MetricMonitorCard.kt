package com.cpumonitor.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.cpumonitor.core.designsystem.model.MetricValueUiState
import com.cpumonitor.core.designsystem.model.MonitorWidgetUiState
import com.cpumonitor.core.designsystem.theme.MonitorDimens

/**
 * Pre-composed monitoring widget card with title, metric, and optional chart slot.
 *
 * @param widget immutable widget state from ViewModel.
 * @param chartContent optional realtime chart composable injected by feature module.
 */
@Composable
fun MetricMonitorCard(
    widget: MonitorWidgetUiState,
    modifier: Modifier = Modifier,
    chartContent: @Composable (() -> Unit)? = null,
    showLiveBadge: Boolean = false,
    compact: Boolean = false,
) {
    MetricMonitorCard(
        title = widget.title,
        subtitle = widget.subtitle,
        metric = widget.primaryMetric,
        modifier = modifier,
        chartContent = chartContent,
        showLiveBadge = showLiveBadge,
        compact = compact,
    )
}

/**
 * Monitoring metric card driven by individual fields.
 */
@Composable
fun MetricMonitorCard(
    title: String,
    metric: MetricValueUiState,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    chartContent: @Composable (() -> Unit)? = null,
    showLiveBadge: Boolean = false,
    compact: Boolean = false,
) {
    MonitorCard(modifier = modifier) {
        MonitorCardHeader(
            title = title,
            subtitle = subtitle,
            trailingContent = if (showLiveBadge && chartContent != null) {
                { LiveIndicatorBadge() }
            } else {
                null
            },
        )
        Spacer(modifier = Modifier.height(MonitorDimens.spacingSm))
        MonitorMetricValue(
            value = metric.value,
            unit = metric.unit,
            label = if (compact) null else metric.label,
            valueColor = metric.accentColor ?: MaterialTheme.colorScheme.primary,
            compact = compact,
        )
        if (chartContent != null) {
            Spacer(modifier = Modifier.height(MonitorDimens.spacingMd))
            chartContent()
        }
    }
}

/**
 * Chart-focused monitoring card with compact header and expanded chart area.
 */
@Composable
fun ChartMonitorCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    headerMetric: MetricValueUiState? = null,
    chartContent: @Composable () -> Unit,
) {
    MonitorCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
        ) {
            MonitorCardHeader(title = title, subtitle = subtitle)
            if (headerMetric != null) {
                MonitorMetricValue(
                    value = headerMetric.value,
                    unit = headerMetric.unit,
                    valueColor = headerMetric.accentColor ?: MaterialTheme.colorScheme.primary,
                )
            }
            chartContent()
        }
    }
}

/**
 * Compact metric tile for dashboard grid layouts (no chart).
 */
@Composable
fun CompactMetricCard(
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
) {
    MonitorCard(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(MonitorDimens.spacingSm))
        MonitorMetricValue(
            value = value,
            unit = unit,
            valueColor = accentColor,
            compact = true,
        )
    }
}
