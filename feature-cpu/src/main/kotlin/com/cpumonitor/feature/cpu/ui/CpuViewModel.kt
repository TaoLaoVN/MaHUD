package com.cpumonitor.feature.cpu.ui

import androidx.lifecycle.viewModelScope
import com.cpumonitor.core.designsystem.model.ChartSeriesUiState
import com.cpumonitor.core.designsystem.model.appendSample
import com.cpumonitor.core.designsystem.theme.MonitorColors
import com.cpumonitor.core.ui.state.UiState
import com.cpumonitor.core.ui.viewmodel.BaseViewModel
import com.cpumonitor.domain.model.CpuArchitectureInfo
import com.cpumonitor.domain.model.CpuProcessorInfo
import com.cpumonitor.domain.model.CpuUsageMetrics
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.metric.CpuMetric
import com.cpumonitor.domain.repository.MetricsRepository
import com.cpumonitor.domain.repository.MonitoringConstants
import com.cpumonitor.domain.usecase.monitoring.ObserveCpuArchitectureUseCase
import com.cpumonitor.domain.usecase.monitoring.ObserveCpuUsageParams
import com.cpumonitor.domain.usecase.monitoring.ObserveCpuUsageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CpuCoreUsageUi(
    val index: Int,
    val usagePercent: Float,
)

data class CpuUiData(
    val totalUsagePercent: Float,
    val bigCoreUsagePercent: Float,
    val littleCoreUsagePercent: Float,
    val perCoreUsage: List<CpuCoreUsageUi>,
    val currentFrequencyMhz: Float,
    val minFrequencyMhz: Float,
    val maxFrequencyMhz: Float,
    val coreCount: Int,
    val abi: String,
    val hardware: String?,
    val processors: List<CpuProcessorInfo>,
    val usageChart: ChartSeriesUiState,
    val historySamples: List<CpuMetric>,
)

private const val CHART_WINDOW = 60
private const val HISTORY_WINDOW_MS = 60_000L

@HiltViewModel
class CpuViewModel @Inject constructor(
    private val observeCpuUsageUseCase: ObserveCpuUsageUseCase,
    private val observeCpuArchitectureUseCase: ObserveCpuArchitectureUseCase,
    private val metricsRepository: MetricsRepository,
) : BaseViewModel() {

    private val intervalMs = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS
    private var usageChart = ChartSeriesUiState(
        id = "cpu_usage",
        label = "CPU Usage",
        values = emptyList(),
        maxPoints = CHART_WINDOW,
        lineColor = MonitorColors.CpuUsage,
    )

    private val _uiState = MutableStateFlow<UiState<CpuUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<CpuUiData>> = _uiState.asStateFlow()

    init {
        startMonitoring()
    }

    private fun startMonitoring() {
        viewModelScope.launch {
            val sinceMillis = System.currentTimeMillis() - HISTORY_WINDOW_MS

            combine(
                observeCpuUsageUseCase(ObserveCpuUsageParams(intervalMs)),
                observeCpuArchitectureUseCase(),
                metricsRepository.observeCpuMetrics(sinceMillis),
            ) { cpuResult, archResult, history ->
                Triple(cpuResult, archResult, history)
            }.collect { (cpuResult, archResult, history) ->
                when (cpuResult) {
                    is Result.Error -> {
                        _uiState.value = UiState.Error(cpuResult.exception.message ?: "CPU monitoring failed")
                        return@collect
                    }
                    Result.Loading -> return@collect
                    is Result.Success -> Unit
                }

                val cpu = (cpuResult as Result.Success).data
                val architecture = (archResult as? Result.Success)?.data

                usageChart = usageChart.appendSample(cpu.totalUsagePercent)

                _uiState.value = UiState.Success(
                    mapToUiData(cpu, architecture, history),
                )
            }
        }
    }

    private fun mapToUiData(
        cpu: CpuUsageMetrics,
        architecture: CpuArchitectureInfo?,
        history: List<CpuMetric>,
    ): CpuUiData {
        val coreCount = cpu.perCoreUsagePercent.size.coerceAtLeast(1)
        val littleCoreCount = coreCount / 2
        val littleCores = cpu.perCoreUsagePercent.take(littleCoreCount)
        val bigCores = cpu.perCoreUsagePercent.drop(littleCoreCount)

        val processors = architecture?.processors.orEmpty()
        val frequencies = processors.mapNotNull { it.currentFrequencyMhz }
        val maxFrequencies = processors.mapNotNull { it.maxFrequencyMhz }

        return CpuUiData(
            totalUsagePercent = cpu.totalUsagePercent,
            bigCoreUsagePercent = bigCores.average().toFloat().takeIf { bigCores.isNotEmpty() }
                ?: cpu.totalUsagePercent,
            littleCoreUsagePercent = littleCores.average().toFloat().takeIf { littleCores.isNotEmpty() }
                ?: cpu.totalUsagePercent,
            perCoreUsage = cpu.perCoreUsagePercent.mapIndexed { index, usage ->
                CpuCoreUsageUi(index = index, usagePercent = usage)
            },
            currentFrequencyMhz = frequencies.average().toFloat().takeIf { frequencies.isNotEmpty() } ?: 0f,
            minFrequencyMhz = maxFrequencies.minOrNull() ?: 0f,
            maxFrequencyMhz = maxFrequencies.maxOrNull() ?: 0f,
            coreCount = architecture?.coreCount ?: coreCount,
            abi = architecture?.abi.orEmpty(),
            hardware = architecture?.hardware,
            processors = processors,
            usageChart = usageChart,
            historySamples = history,
        )
    }
}
