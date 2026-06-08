package com.cpumonitor.feature.cpu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cpumonitor.core.charts.RealtimeLineChart
import com.cpumonitor.core.designsystem.component.MetricMonitorCard
import com.cpumonitor.core.designsystem.component.MonitorCard
import com.cpumonitor.core.designsystem.component.MonitorCardHeader
import com.cpumonitor.core.designsystem.component.MonitorMetricValue
import com.cpumonitor.core.designsystem.model.MetricValueUiState
import com.cpumonitor.core.designsystem.model.toFrequencyMhzDisplay
import com.cpumonitor.core.designsystem.model.toPercentDisplay
import com.cpumonitor.core.designsystem.theme.MonitorColors
import com.cpumonitor.core.designsystem.theme.MonitorDimens
import com.cpumonitor.core.ui.R
import com.cpumonitor.core.ui.state.UiState

@Composable
fun CpuScreen(
    viewModel: CpuViewModel = hiltViewModel(),
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

            is UiState.Success -> CpuContent(
                data = state.data,
                contentPadding = padding,
            )
        }
    }
}

@Composable
private fun CpuContent(
    data: CpuUiData,
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
            MetricMonitorCard(
                widget = com.cpumonitor.core.designsystem.model.MonitorWidgetUiState(
                    id = "cpu_total",
                    title = stringResource(R.string.widget_cpu_usage),
                    subtitle = stringResource(R.string.subtitle_realtime),
                    primaryMetric = MetricValueUiState(
                        label = stringResource(R.string.label_total),
                        value = data.totalUsagePercent.toPercentDisplay(),
                        unit = "%",
                        accentColor = MonitorColors.CpuUsage,
                    ),
                    chartSeries = data.usageChart,
                ),
                chartContent = { RealtimeLineChart(series = data.usageChart) },
            )
        }

        item {
            MonitorCard {
                MonitorCardHeader(
                    title = stringResource(R.string.cpu_core_clusters),
                    subtitle = stringResource(R.string.cpu_big_little),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MonitorDimens.spacingMd),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    MonitorMetricValue(
                        label = stringResource(R.string.cpu_big_cores),
                        value = data.bigCoreUsagePercent.toPercentDisplay(),
                        unit = "%",
                        valueColor = MonitorColors.CpuUsage,
                    )
                    MonitorMetricValue(
                        label = stringResource(R.string.cpu_little_cores),
                        value = data.littleCoreUsagePercent.toPercentDisplay(),
                        unit = "%",
                        valueColor = MonitorColors.CpuFrequency,
                    )
                }
            }
        }

        items(data.perCoreUsage, key = { it.index }) { core ->
            MonitorCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.cpu_core_index, core.index),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "${core.usagePercent.toPercentDisplay()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MonitorColors.CpuUsage,
                    )
                }
                LinearProgressIndicator(
                    progress = { (core.usagePercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MonitorDimens.spacingSm),
                    color = MonitorColors.CpuUsage,
                )
            }
        }

        item {
            MonitorCard {
                MonitorCardHeader(
                    title = stringResource(R.string.cpu_frequency),
                    subtitle = stringResource(R.string.cpu_frequency_subtitle),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MonitorDimens.spacingMd),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    MonitorMetricValue(
                        label = stringResource(R.string.label_current),
                        value = data.currentFrequencyMhz.toFrequencyMhzDisplay(),
                        unit = "MHz",
                    )
                    MonitorMetricValue(
                        label = stringResource(R.string.label_min),
                        value = data.minFrequencyMhz.toFrequencyMhzDisplay(),
                        unit = "MHz",
                    )
                    MonitorMetricValue(
                        label = stringResource(R.string.label_max),
                        value = data.maxFrequencyMhz.toFrequencyMhzDisplay(),
                        unit = "MHz",
                    )
                }
            }
        }

        item {
            MonitorCard {
                MonitorCardHeader(
                    title = stringResource(R.string.cpu_architecture),
                    subtitle = stringResource(R.string.cpu_hardware_info),
                )
                Column(
                    modifier = Modifier.padding(top = MonitorDimens.spacingMd),
                    verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingXs),
                ) {
                    ArchitectureRow(stringResource(R.string.cpu_cores), data.coreCount.toString())
                    ArchitectureRow(stringResource(R.string.cpu_abi), data.abi.ifEmpty { "—" })
                    ArchitectureRow(stringResource(R.string.cpu_hardware), data.hardware ?: "—")
                    data.processors.firstOrNull()?.let { processor ->
                        processor.architecture?.let {
                            ArchitectureRow(stringResource(R.string.cpu_arm_arch), it)
                        }
                        processor.implementer?.let {
                            ArchitectureRow(stringResource(R.string.cpu_implementer), it)
                        }
                        processor.part?.let {
                            ArchitectureRow(stringResource(R.string.cpu_part), it)
                        }
                    }
                }
            }
        }

        item {
            MonitorCard {
                MonitorCardHeader(
                    title = stringResource(R.string.cpu_history),
                    subtitle = stringResource(R.string.cpu_history_subtitle, data.historySamples.size),
                )
                if (data.historySamples.isEmpty()) {
                    Text(
                        text = stringResource(R.string.cpu_no_samples),
                        modifier = Modifier.padding(top = MonitorDimens.spacingMd),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    val avgHistory = data.historySamples
                        .map { it.totalUsagePercent }
                        .average()
                        .toFloat()
                    MonitorMetricValue(
                        modifier = Modifier.padding(top = MonitorDimens.spacingMd),
                        label = stringResource(R.string.label_average),
                        value = avgHistory.toPercentDisplay(),
                        unit = "%",
                        valueColor = MonitorColors.CpuUsage,
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchitectureRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
