package com.cpumonitor.feature.thermal.ui

import androidx.lifecycle.viewModelScope
import com.cpumonitor.core.designsystem.model.ChartSeriesUiState
import com.cpumonitor.core.designsystem.model.appendSample
import com.cpumonitor.core.designsystem.theme.MonitorColors
import com.cpumonitor.core.ui.state.UiState
import com.cpumonitor.core.ui.viewmodel.BaseViewModel
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.ThermalMetrics
import com.cpumonitor.domain.repository.MonitoringConstants
import com.cpumonitor.domain.repository.ThermalRepository
import com.cpumonitor.domain.usecase.monitoring.ObserveThermalParams
import com.cpumonitor.domain.usecase.monitoring.ObserveThermalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ThermalZoneUi(
    val name: String,
    val temperatureCelsius: Float,
)

data class ThermalUiData(
    val cpuTemperatureCelsius: Float,
    val batteryTemperatureCelsius: Float,
    val thermalZones: List<ThermalZoneUi>,
    val isOverheating: Boolean,
    val isThrottling: Boolean,
    val temperatureChart: ChartSeriesUiState,
)

private const val CHART_WINDOW = 60
private const val OVERHEATING_THRESHOLD_CELSIUS = 45f

@HiltViewModel
class ThermalViewModel @Inject constructor(
    private val observeThermalUseCase: ObserveThermalUseCase,
    private val thermalRepository: ThermalRepository,
) : BaseViewModel() {

    private val intervalMs = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS
    private var temperatureChart = ChartSeriesUiState(
        id = "thermal_cpu",
        label = "CPU Temperature",
        values = emptyList(),
        maxPoints = CHART_WINDOW,
        lineColor = MonitorColors.Thermal,
    )

    private var cachedZones: List<ThermalZoneUi> = emptyList()

    private val _uiState = MutableStateFlow<UiState<ThermalUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<ThermalUiData>> = _uiState.asStateFlow()

    init {
        loadThermalZones()
        startMonitoring()
    }

    private fun loadThermalZones() {
        viewModelScope.launch {
            when (val result = thermalRepository.getThermalZones()) {
                is Result.Success -> cachedZones = result.data.toThermalZoneList()
                else -> Unit
            }
        }
    }

    private fun startMonitoring() {
        viewModelScope.launch {
            observeThermalUseCase(ObserveThermalParams(intervalMs)).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val thermal = result.data
                        temperatureChart = temperatureChart.appendSample(thermal.cpuTemperatureCelsius)
                        _uiState.value = UiState.Success(mapToUiData(thermal, cachedZones))
                    }
                    is Result.Error -> {
                        _uiState.value = UiState.Error(
                            result.exception.message ?: "Thermal monitoring failed",
                        )
                    }
                    Result.Loading -> Unit
                }
            }
        }
    }

    private fun mapToUiData(
        thermal: ThermalMetrics,
        zones: List<ThermalZoneUi>,
    ): ThermalUiData =
        ThermalUiData(
            cpuTemperatureCelsius = thermal.cpuTemperatureCelsius,
            batteryTemperatureCelsius = thermal.batteryTemperatureCelsius,
            thermalZones = zones,
            isOverheating = thermal.cpuTemperatureCelsius > OVERHEATING_THRESHOLD_CELSIUS,
            isThrottling = false,
            temperatureChart = temperatureChart,
        )

    private fun Map<String, Float>.toThermalZoneList(): List<ThermalZoneUi> =
        entries
            .sortedByDescending { it.value }
            .map { (name, temp) ->
                ThermalZoneUi(name = name, temperatureCelsius = temp)
            }
}
