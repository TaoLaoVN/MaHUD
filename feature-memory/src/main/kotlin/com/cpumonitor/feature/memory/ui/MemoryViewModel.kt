package com.cpumonitor.feature.memory.ui

import androidx.lifecycle.viewModelScope
import com.cpumonitor.core.designsystem.model.ChartSeriesUiState
import com.cpumonitor.core.designsystem.model.appendSample
import com.cpumonitor.core.designsystem.theme.MonitorColors
import com.cpumonitor.core.ui.state.UiState
import com.cpumonitor.core.ui.viewmodel.BaseViewModel
import com.cpumonitor.domain.model.MemoryMetrics
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.repository.MonitoringConstants
import com.cpumonitor.domain.usecase.monitoring.ObserveMemoryUsageParams
import com.cpumonitor.domain.usecase.monitoring.ObserveMemoryUsageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

data class MemoryUiData(
    val usedBytes: Long,
    val availableBytes: Long,
    val freeBytes: Long,
    val cachedBytes: Long,
    val totalBytes: Long,
    val usedPercent: Float,
    val isLowMemory: Boolean,
    val usageChart: ChartSeriesUiState,
)

private const val CHART_WINDOW = 60

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val observeMemoryUsageUseCase: ObserveMemoryUsageUseCase,
) : BaseViewModel() {

    private val intervalMs = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS
    private var usageChart = ChartSeriesUiState(
        id = "memory_usage",
        label = "Memory Usage",
        values = emptyList(),
        maxPoints = CHART_WINDOW,
        lineColor = MonitorColors.MemoryUsage,
    )

    private val _uiState = MutableStateFlow<UiState<MemoryUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<MemoryUiData>> = _uiState.asStateFlow()

    init {
        startMonitoring()
    }

    private fun startMonitoring() {
        viewModelScope.launch {
            observeMemoryUsageUseCase(ObserveMemoryUsageParams(intervalMs)).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val memory = result.data
                        usageChart = usageChart.appendSample(toUsedPercent(memory))
                        _uiState.value = UiState.Success(mapToUiData(memory))
                    }
                    is Result.Error -> {
                        _uiState.value = UiState.Error(
                            result.exception.message ?: "Memory monitoring failed",
                        )
                    }
                    Result.Loading -> Unit
                }
            }
        }
    }

    private fun mapToUiData(memory: MemoryMetrics): MemoryUiData =
        MemoryUiData(
            usedBytes = memory.usedBytes,
            availableBytes = memory.availableBytes,
            freeBytes = memory.freeBytes,
            cachedBytes = memory.cachedBytes,
            totalBytes = memory.totalBytes,
            usedPercent = toUsedPercent(memory),
            isLowMemory = memory.isLowMemory,
            usageChart = usageChart,
        )

    private fun toUsedPercent(memory: MemoryMetrics): Float =
        if (memory.totalBytes <= 0L) {
            0f
        } else {
            ((memory.usedBytes.toDouble() / memory.totalBytes.toDouble()) * 100.0)
                .roundToInt()
                .toFloat()
        }
}
