package com.cpumonitor.feature.battery.ui

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
import com.cpumonitor.core.ui.localized.localizedBatteryHealth
import com.cpumonitor.core.ui.state.UiState

@Composable
fun BatteryScreen(
    viewModel: BatteryViewModel = hiltViewModel(),
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

            is UiState.Success -> BatteryContent(
                data = state.data,
                contentPadding = padding,
            )
        }
    }
}

@Composable
private fun BatteryContent(
    data: BatteryUiData,
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
                    id = "battery_level",
                    title = stringResource(R.string.battery_level),
                    subtitle = if (data.isCharging) {
                        stringResource(R.string.subtitle_charging)
                    } else {
                        stringResource(R.string.subtitle_discharging)
                    },
                    primaryMetric = MetricValueUiState(
                        label = stringResource(R.string.label_level),
                        value = data.percentage.toString(),
                        unit = "%",
                        accentColor = MonitorColors.BatteryLevel,
                    ),
                    chartSeries = data.levelChart,
                ),
                chartContent = { RealtimeLineChart(series = data.levelChart) },
            )
        }

        item {
            MonitorCard {
                MonitorCardHeader(
                    title = stringResource(R.string.battery_electrical),
                    subtitle = stringResource(R.string.battery_voltage_current),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MonitorDimens.spacingMd),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    MonitorMetricValue(
                        label = stringResource(R.string.label_voltage),
                        value = "%.2f".format(data.voltageMv / 1_000f),
                        unit = "V",
                    )
                    MonitorMetricValue(
                        label = stringResource(R.string.label_electric_current),
                        value = data.currentMa.toString(),
                        unit = "mA",
                    )
                }
            }
        }

        item {
            MonitorCard {
                MonitorCardHeader(title = stringResource(R.string.battery_status))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MonitorDimens.spacingMd),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    MonitorMetricValue(
                        label = stringResource(R.string.label_temperature),
                        value = data.temperatureCelsius.toPercentDisplay(0),
                        unit = "°C",
                    )
                    MonitorMetricValue(
                        label = stringResource(R.string.label_health),
                        value = data.health.localizedBatteryHealth(),
                        unit = "",
                    )
                }
                if (data.isCharging && data.chargeSpeedMw != null) {
                    MonitorMetricValue(
                        modifier = Modifier.padding(top = MonitorDimens.spacingMd),
                        label = stringResource(R.string.label_charge_speed),
                        value = data.chargeSpeedMw.toString(),
                        unit = "mW",
                        valueColor = MonitorColors.BatteryLevel,
                    )
                }
            }
        }
    }
}
