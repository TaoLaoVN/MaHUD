package com.cpumonitor.feature.history.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.cpumonitor.core.ui.R
import com.cpumonitor.core.ui.localized.localizedName
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cpumonitor.core.charts.RealtimeLineChart
import com.cpumonitor.core.designsystem.component.MetricMonitorCard
import com.cpumonitor.core.designsystem.component.MonitorCard
import com.cpumonitor.core.designsystem.component.MonitorCardHeader
import com.cpumonitor.core.designsystem.model.MetricValueUiState
import com.cpumonitor.core.designsystem.model.toPercentDisplay
import com.cpumonitor.core.designsystem.theme.MonitorDimens
import com.cpumonitor.core.ui.state.UiState
import com.cpumonitor.domain.model.history.HistoryMetricType
import com.cpumonitor.domain.model.history.HistoryTimeRange

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val metricType by viewModel.selectedMetricType.collectAsStateWithLifecycle()
    val timeRange by viewModel.selectedTimeRange.collectAsStateWithLifecycle()

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

            is UiState.Success -> HistoryContent(
                data = state.data,
                selectedMetricType = metricType,
                selectedTimeRange = timeRange,
                onMetricTypeSelected = viewModel::selectMetricType,
                onTimeRangeSelected = viewModel::selectTimeRange,
                contentPadding = padding,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HistoryContent(
    data: HistoryUiData,
    selectedMetricType: HistoryMetricType,
    selectedTimeRange: HistoryTimeRange,
    onMetricTypeSelected: (HistoryMetricType) -> Unit,
    onTimeRangeSelected: (HistoryTimeRange) -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
    ) {
        item {
            MonitorCard {
                MonitorCardHeader(
                    title = stringResource(R.string.history_metric),
                    subtitle = stringResource(R.string.history_select_source),
                )
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MonitorDimens.spacingMd),
                    horizontalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
                ) {
                    HistoryMetricType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedMetricType == type,
                            onClick = { onMetricTypeSelected(type) },
                            label = { Text(type.localizedName()) },
                        )
                    }
                }
            }
        }

        item {
            MonitorCard {
                MonitorCardHeader(
                    title = stringResource(R.string.history_time_range),
                    subtitle = data.timeRange.localizedName(),
                )
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MonitorDimens.spacingMd),
                    horizontalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
                ) {
                    HistoryTimeRange.entries.forEach { range ->
                        FilterChip(
                            selected = selectedTimeRange == range,
                            onClick = { onTimeRangeSelected(range) },
                            label = { Text(range.localizedName()) },
                        )
                    }
                }
            }
        }

        item {
            if (data.sampleCount == 0) {
                MonitorCard {
                    MonitorCardHeader(
                        title = stringResource(R.string.history_no_data),
                        subtitle = stringResource(R.string.history_no_data_subtitle),
                    )
                }
            } else {
                val latest = data.chartSeries.latestValue ?: 0f
                MetricMonitorCard(
                    widget = com.cpumonitor.core.designsystem.model.MonitorWidgetUiState(
                        id = "history_chart",
                        title = stringResource(R.string.history_title, data.metricType.localizedName()),
                        subtitle = stringResource(R.string.history_samples, data.sampleCount),
                        primaryMetric = MetricValueUiState(
                            label = stringResource(R.string.label_latest),
                            value = if (data.unit == "°C") {
                                latest.toPercentDisplay(0)
                            } else {
                                latest.toPercentDisplay()
                            },
                            unit = data.unit,
                            accentColor = data.chartSeries.lineColor,
                        ),
                        chartSeries = data.chartSeries,
                    ),
                    chartContent = { RealtimeLineChart(series = data.chartSeries, showAxis = true) },
                )
            }
        }
    }
}
