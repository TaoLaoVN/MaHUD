package com.cpumonitor.feature.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cpumonitor.core.charts.RealtimeLineChart
import com.cpumonitor.core.designsystem.component.ChartMonitorCard
import com.cpumonitor.core.designsystem.component.CompactMetricCard
import com.cpumonitor.core.designsystem.component.MetricMonitorCard
import com.cpumonitor.core.designsystem.layout.horizontalContentPadding
import com.cpumonitor.core.designsystem.layout.rememberMonitorWindowSize
import com.cpumonitor.core.designsystem.model.MonitorWidgetUiState
import com.cpumonitor.core.designsystem.theme.MonitorDimens
import com.cpumonitor.core.ui.R
import com.cpumonitor.core.ui.state.UiState

private const val WIDGET_CPU = "widget_cpu"
private const val WIDGET_MEMORY = "widget_memory"
private const val WIDGET_THERMAL = "widget_thermal"
private const val WIDGET_FREQUENCY = "widget_frequency"
private const val WIDGET_BATTERY = "widget_battery"
private const val WIDGET_HEALTH = "widget_health"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToStorage: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToExport: () -> Unit = {},
    onNavigateToProcess: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {},
    onNavigateToOverlay: () -> Unit = {},
    onNavigateToBenchmark: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.app_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (uiState is UiState.Success) {
                            Text(
                                text = formatLastUpdated((uiState as UiState.Success).data.lastUpdatedEpochMs),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    TextButton(onClick = onNavigateToSettings) {
                        Text(stringResource(R.string.settings))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            is UiState.Error -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(MonitorDimens.spacingXl),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            is UiState.Success -> DashboardContent(
                widgets = state.data.widgets,
                modifier = Modifier.fillMaxSize(),
                contentPadding = padding,
            )
        }
    }
}

@Composable
internal fun DashboardContent(
    widgets: List<MonitorWidgetUiState>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val windowSize = rememberMonitorWindowSize()
    val horizontalPadding = windowSize.horizontalContentPadding()
    val widgetMap = widgets.associateBy { it.id }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = horizontalPadding + contentPadding.calculateLeftPadding(
                androidx.compose.ui.unit.LayoutDirection.Ltr,
            ),
            end = horizontalPadding + contentPadding.calculateRightPadding(
                androidx.compose.ui.unit.LayoutDirection.Ltr,
            ),
            top = MonitorDimens.spacingSm + contentPadding.calculateTopPadding(),
            bottom = MonitorDimens.spacingMd + contentPadding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingMd),
    ) {
        item {
            DashboardSectionHeader(title = stringResource(R.string.section_overview))
        }

        item {
            OverviewGrid(widgetMap = widgetMap)
        }

        item {
            DashboardSectionHeader(title = stringResource(R.string.section_live_charts))
        }

        widgetMap[WIDGET_CPU]?.let { widget ->
            item {
                ChartMonitorCard(
                    title = widget.title,
                    subtitle = widget.subtitle,
                    headerMetric = widget.primaryMetric,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    widget.chartSeries?.let { series ->
                        RealtimeLineChart(
                            series = series,
                            chartHeight = MonitorDimens.chartHeightLarge,
                        )
                    }
                }
            }
        }

        widgetMap[WIDGET_MEMORY]?.let { widget ->
            item {
                ChartMonitorCard(
                    title = widget.title,
                    subtitle = widget.subtitle,
                    headerMetric = widget.primaryMetric,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    widget.chartSeries?.let { series ->
                        RealtimeLineChart(series = series)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MonitorDimens.spacingMd),
            ) {
                listOf(WIDGET_THERMAL, WIDGET_BATTERY).forEach { widgetId ->
                    widgetMap[widgetId]?.let { widget ->
                        MetricMonitorCard(
                            widget = widget,
                            modifier = Modifier.weight(1f),
                            compact = true,
                            chartContent = widget.chartSeries?.let { series ->
                                {
                                    RealtimeLineChart(
                                        series = series,
                                        chartHeight = MonitorDimens.chartHeight,
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewGrid(widgetMap: Map<String, MonitorWidgetUiState>) {
    val overviewIds = listOf(
        WIDGET_HEALTH,
        WIDGET_BATTERY,
        WIDGET_FREQUENCY,
        WIDGET_THERMAL,
    )

    Column(verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm)) {
        overviewIds.chunked(2).forEach { rowIds ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
            ) {
                rowIds.forEach { id ->
                    val widget = widgetMap[id]
                    if (widget != null) {
                        CompactMetricCard(
                            title = widget.title,
                            value = widget.primaryMetric.value,
                            unit = widget.primaryMetric.unit,
                            accentColor = widget.primaryMetric.accentColor
                                ?: MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
                if (rowIds.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DashboardSectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(top = MonitorDimens.spacingSm),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun formatLastUpdated(epochMs: Long): String {
    val seconds = ((System.currentTimeMillis() - epochMs) / 1_000L).coerceAtLeast(0L)
    return when {
        seconds < 5L -> stringResource(R.string.updated_just_now)
        seconds < 60L -> stringResource(R.string.updated_seconds_ago, seconds)
        else -> stringResource(R.string.updated_minutes_ago, seconds / 60L)
    }
}
