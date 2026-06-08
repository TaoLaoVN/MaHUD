package com.cpumonitor.feature.memory.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cpumonitor.core.charts.RealtimeLineChart
import com.cpumonitor.core.designsystem.component.MetricMonitorCard
import com.cpumonitor.core.designsystem.component.MonitorCard
import com.cpumonitor.core.designsystem.component.MonitorCardHeader
import com.cpumonitor.core.designsystem.component.MonitorMetricValue
import com.cpumonitor.core.designsystem.model.MetricValueUiState
import com.cpumonitor.core.designsystem.model.toPercentDisplay
import com.cpumonitor.core.designsystem.theme.MonitorColors
import com.cpumonitor.core.designsystem.theme.MonitorDimens
import com.cpumonitor.core.ui.R
import com.cpumonitor.core.ui.state.UiState

@Composable
fun MemoryScreen(
    viewModel: MemoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )

            is UiState.Error -> Text(
                text = state.message,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                color = MaterialTheme.colorScheme.error,
            )

            is UiState.Success -> MemoryContent(
                data = state.data,
                contentPadding = padding,
            )
        }
    }
}

@Composable
private fun MemoryContent(
    data: MemoryUiData,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = MonitorDimens.spacingLg + contentPadding.calculateLeftPadding(
                androidx.compose.ui.unit.LayoutDirection.Ltr,
            ),
            end = MonitorDimens.spacingLg + contentPadding.calculateRightPadding(
                androidx.compose.ui.unit.LayoutDirection.Ltr,
            ),
            top = MonitorDimens.spacingLg + contentPadding.calculateTopPadding(),
            bottom = MonitorDimens.spacingLg + contentPadding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingMd),
    ) {
        item {
            MetricMonitorCard(
                widget = com.cpumonitor.core.designsystem.model.MonitorWidgetUiState(
                    id = "memory_usage",
                    title = stringResource(R.string.memory_usage),
                    subtitle = if (data.isLowMemory) {
                        stringResource(R.string.memory_low_warning)
                    } else {
                        stringResource(R.string.subtitle_realtime)
                    },
                    primaryMetric = MetricValueUiState(
                        label = stringResource(R.string.label_used),
                        value = data.usedPercent.toPercentDisplay(),
                        unit = "%",
                        accentColor = MonitorColors.MemoryUsage,
                    ),
                    chartSeries = data.usageChart,
                ),
                chartContent = { RealtimeLineChart(series = data.usageChart) },
            )
        }

        item {
            MonitorCard {
                MonitorCardHeader(title = stringResource(R.string.memory_breakdown))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MonitorDimens.spacingMd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    MonitorMetricValue(
                        label = stringResource(R.string.label_used),
                        value = formatBytes(data.usedBytes),
                        unit = "",
                    )
                    MonitorMetricValue(
                        label = stringResource(R.string.label_available),
                        value = formatBytes(data.availableBytes),
                        unit = "",
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MonitorDimens.spacingMd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    MonitorMetricValue(
                        label = stringResource(R.string.label_free),
                        value = formatBytes(data.freeBytes),
                        unit = "",
                    )
                    MonitorMetricValue(
                        label = stringResource(R.string.label_cached),
                        value = formatBytes(data.cachedBytes),
                        unit = "",
                    )
                }
                Text(
                    text = stringResource(R.string.memory_total_fmt, formatBytes(data.totalBytes)),
                    modifier = Modifier.padding(top = MonitorDimens.spacingSm),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

internal fun formatBytes(bytes: Long): String {
    if (bytes < 1_024) return "$bytes B"
    val kb = bytes / 1_024.0
    if (kb < 1_024) return "%.1f KB".format(kb)
    val mb = kb / 1_024.0
    if (mb < 1_024) return "%.1f MB".format(mb)
    val gb = mb / 1_024.0
    return "%.2f GB".format(gb)
}
