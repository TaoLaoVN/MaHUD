package com.cpumonitor.feature.history.ui

import androidx.lifecycle.viewModelScope
import com.cpumonitor.core.designsystem.model.ChartSeriesUiState
import com.cpumonitor.core.designsystem.theme.MonitorColors
import com.cpumonitor.core.ui.state.UiState
import com.cpumonitor.core.ui.viewmodel.BaseViewModel
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.history.HistoryMetricType
import com.cpumonitor.domain.model.history.HistoryTimeRange
import com.cpumonitor.domain.usecase.history.HistoryChartPoint
import com.cpumonitor.domain.usecase.history.ObserveMetricHistoryParams
import com.cpumonitor.domain.usecase.history.ObserveMetricHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiData(
    val metricType: HistoryMetricType,
    val timeRange: HistoryTimeRange,
    val sampleCount: Int,
    val chartSeries: ChartSeriesUiState,
    val unit: String,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val observeMetricHistoryUseCase: ObserveMetricHistoryUseCase,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<UiState<HistoryUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<HistoryUiData>> = _uiState.asStateFlow()

    private val _selectedMetricType = MutableStateFlow(HistoryMetricType.CPU)
    val selectedMetricType: StateFlow<HistoryMetricType> = _selectedMetricType.asStateFlow()

    private val _selectedTimeRange = MutableStateFlow(HistoryTimeRange.ONE_HOUR)
    val selectedTimeRange: StateFlow<HistoryTimeRange> = _selectedTimeRange.asStateFlow()

    private var observeJob: Job? = null

    init {
        restartObservation()
    }

    fun selectMetricType(type: HistoryMetricType) {
        if (_selectedMetricType.value == type) return
        _selectedMetricType.value = type
        restartObservation()
    }

    fun selectTimeRange(range: HistoryTimeRange) {
        if (_selectedTimeRange.value == range) return
        _selectedTimeRange.value = range
        restartObservation()
    }

    private fun restartObservation() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.value = UiState.Loading
            val params = ObserveMetricHistoryParams(
                metricType = _selectedMetricType.value,
                timeRange = _selectedTimeRange.value,
            )
            observeMetricHistoryUseCase(params).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.value = UiState.Success(mapToUiData(result.data))
                    }
                    is Result.Error -> {
                        _uiState.value = UiState.Error(
                            result.exception.message ?: "History query failed",
                        )
                    }
                    Result.Loading -> Unit
                }
            }
        }
    }

    private fun mapToUiData(points: List<HistoryChartPoint>): HistoryUiData {
        val metricType = _selectedMetricType.value
        val values = points.map { it.value }
        return HistoryUiData(
            metricType = metricType,
            timeRange = _selectedTimeRange.value,
            sampleCount = points.size,
            chartSeries = ChartSeriesUiState(
                id = "history_${metricType.name}",
                label = metricType.displayName,
                values = values,
                maxPoints = values.size.coerceAtLeast(1),
                lineColor = chartColor(metricType),
            ),
            unit = chartUnit(metricType),
        )
    }

    private fun chartColor(metricType: HistoryMetricType) = when (metricType) {
        HistoryMetricType.CPU -> MonitorColors.CpuUsage
        HistoryMetricType.MEMORY -> MonitorColors.MemoryUsage
        HistoryMetricType.THERMAL -> MonitorColors.Thermal
        HistoryMetricType.BATTERY -> MonitorColors.BatteryLevel
    }

    private fun chartUnit(metricType: HistoryMetricType) = when (metricType) {
        HistoryMetricType.CPU -> "%"
        HistoryMetricType.MEMORY -> "%"
        HistoryMetricType.THERMAL -> "°C"
        HistoryMetricType.BATTERY -> "%"
    }
}
