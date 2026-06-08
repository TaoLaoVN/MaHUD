package com.cpumonitor.feature.thermal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.cpumonitor.core.ui.localized.localizedThermalZoneName
import com.cpumonitor.core.ui.state.UiState

@Composable
fun ThermalScreen(
    viewModel: ThermalViewModel = hiltViewModel(),
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

            is UiState.Success -> ThermalContent(
                data = state.data,
                contentPadding = padding,
            )
        }
    }
}

@Composable
private fun ThermalContent(
    data: ThermalUiData,
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
                    id = "thermal_cpu",
                    title = stringResource(R.string.widget_cpu_temp),
                    subtitle = when {
                        data.isOverheating -> stringResource(R.string.thermal_overheating)
                        data.isThrottling -> stringResource(R.string.thermal_throttling)
                        else -> stringResource(R.string.subtitle_realtime)
                    },
                    primaryMetric = MetricValueUiState(
                        label = stringResource(R.string.label_current),
                        value = data.cpuTemperatureCelsius.toPercentDisplay(0),
                        unit = "°C",
                        accentColor = MonitorColors.Thermal,
                    ),
                    chartSeries = data.temperatureChart,
                ),
                chartContent = { RealtimeLineChart(series = data.temperatureChart) },
            )
        }

        item {
            MonitorCard {
                MonitorCardHeader(title = stringResource(R.string.thermal_temperatures))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MonitorDimens.spacingMd),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    MonitorMetricValue(
                        label = stringResource(R.string.metric_cpu),
                        value = data.cpuTemperatureCelsius.toPercentDisplay(0),
                        unit = "°C",
                        valueColor = MonitorColors.Thermal,
                    )
                    MonitorMetricValue(
                        label = stringResource(R.string.metric_battery),
                        value = data.batteryTemperatureCelsius.toPercentDisplay(0),
                        unit = "°C",
                        valueColor = MonitorColors.BatteryLevel,
                    )
                }
            }
        }

        if (data.thermalZones.isNotEmpty()) {
            item {
                MonitorCard {
                    MonitorCardHeader(
                        title = stringResource(R.string.thermal_zones),
                        subtitle = stringResource(R.string.thermal_zones_count, data.thermalZones.size),
                    )
                }
            }

            items(data.thermalZones, key = { it.name }) { zone ->
                MonitorCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = zone.name.localizedThermalZoneName(),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "${zone.temperatureCelsius.toPercentDisplay(0)}°C",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MonitorColors.Thermal,
                        )
                    }
                }
            }
        }
    }
}
