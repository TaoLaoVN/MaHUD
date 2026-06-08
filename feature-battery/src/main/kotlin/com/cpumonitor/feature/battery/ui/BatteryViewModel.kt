package com.cpumonitor.feature.battery.ui

import androidx.lifecycle.viewModelScope
import com.cpumonitor.core.designsystem.model.ChartSeriesUiState
import com.cpumonitor.core.designsystem.model.appendSample
import com.cpumonitor.core.designsystem.theme.MonitorColors
import com.cpumonitor.core.ui.state.UiState
import com.cpumonitor.core.ui.viewmodel.BaseViewModel
import com.cpumonitor.domain.model.BatteryMetrics
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.repository.MonitoringConstants
import com.cpumonitor.domain.usecase.monitoring.ObserveBatteryStatusParams
import com.cpumonitor.domain.usecase.monitoring.ObserveBatteryStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BatteryUiData(
    val percentage: Int,
    val voltageMv: Int,
    val currentMa: Int,
    val temperatureCelsius: Float,
    val health: String,
    val isCharging: Boolean,
    val chargeSpeedMw: Int?,
    val levelChart: ChartSeriesUiState,
)

private const val CHART_WINDOW = 60

@HiltViewModel
class BatteryViewModel @Inject constructor(
    private val observeBatteryStatusUseCase: ObserveBatteryStatusUseCase,
) : BaseViewModel() {

    private val intervalMs = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS
    private var levelChart = ChartSeriesUiState(
        id = "battery_level",
        label = "Battery Level",
        values = emptyList(),
        maxPoints = CHART_WINDOW,
        lineColor = MonitorColors.BatteryLevel,
    )

    private val _uiState = MutableStateFlow<UiState<BatteryUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<BatteryUiData>> = _uiState.asStateFlow()

    init {
        startMonitoring()
    }

    private fun startMonitoring() {
        viewModelScope.launch {
            observeBatteryStatusUseCase(ObserveBatteryStatusParams(intervalMs)).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val battery = result.data
                        levelChart = levelChart.appendSample(battery.percentage.toFloat())
                        _uiState.value = UiState.Success(mapToUiData(battery))
                    }
                    is Result.Error -> {
                        _uiState.value = UiState.Error(
                            result.exception.message ?: "Battery monitoring failed",
                        )
                    }
                    Result.Loading -> Unit
                }
            }
        }
    }

    private fun mapToUiData(battery: BatteryMetrics): BatteryUiData =
        BatteryUiData(
            percentage = battery.percentage,
            voltageMv = battery.voltageMv,
            currentMa = battery.currentMa,
            temperatureCelsius = battery.temperatureCelsius,
            health = battery.health,
            isCharging = battery.isCharging,
            chargeSpeedMw = battery.chargeSpeedMw,
            levelChart = levelChart,
        )
}
