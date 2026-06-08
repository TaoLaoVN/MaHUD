package com.cpumonitor.feature.dashboard.ui



import android.content.Context
import androidx.lifecycle.viewModelScope
import com.cpumonitor.core.designsystem.model.ChartSeriesUiState
import com.cpumonitor.core.ui.R

import com.cpumonitor.core.designsystem.model.MetricValueUiState

import com.cpumonitor.core.designsystem.model.MonitorWidgetUiState

import com.cpumonitor.core.designsystem.model.appendSample

import com.cpumonitor.core.designsystem.model.toFrequencyMhzDisplay

import com.cpumonitor.core.designsystem.model.toPercentDisplay

import com.cpumonitor.core.designsystem.theme.MonitorColors

import com.cpumonitor.core.ui.state.UiState

import com.cpumonitor.core.ui.viewmodel.BaseViewModel

import com.cpumonitor.domain.model.BatteryMetrics

import com.cpumonitor.domain.model.CpuArchitectureInfo

import com.cpumonitor.domain.model.CpuUsageMetrics

import com.cpumonitor.domain.model.MemoryMetrics

import com.cpumonitor.domain.model.Result

import com.cpumonitor.domain.model.ThermalMetrics

import com.cpumonitor.domain.model.analytics.DeviceHealthReport
import com.cpumonitor.domain.model.analytics.HealthStatus
import com.cpumonitor.domain.repository.MonitoringConstants
import dagger.hilt.android.qualifiers.ApplicationContext

import com.cpumonitor.domain.usecase.monitoring.ObserveBatteryStatusParams

import com.cpumonitor.domain.usecase.monitoring.ObserveBatteryStatusUseCase

import com.cpumonitor.domain.usecase.monitoring.ObserveCpuArchitectureUseCase

import com.cpumonitor.domain.usecase.monitoring.ObserveCpuUsageParams

import com.cpumonitor.domain.usecase.monitoring.ObserveCpuUsageUseCase

import com.cpumonitor.domain.usecase.monitoring.ObserveMemoryUsageParams

import com.cpumonitor.domain.usecase.monitoring.ObserveMemoryUsageUseCase

import com.cpumonitor.domain.usecase.monitoring.ObserveThermalParams

import com.cpumonitor.domain.usecase.monitoring.ObserveThermalUseCase

import com.cpumonitor.domain.usecase.analytics.ObserveDeviceHealthParams

import com.cpumonitor.domain.usecase.analytics.ObserveDeviceHealthUseCase

import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.flow.MutableStateFlow

import kotlinx.coroutines.flow.StateFlow

import kotlinx.coroutines.flow.asStateFlow

import kotlinx.coroutines.flow.combine

import kotlinx.coroutines.flow.update

import kotlinx.coroutines.launch

import javax.inject.Inject

import kotlin.math.roundToInt



/**

 * Immutable dashboard payload exposed to Compose.

 */

data class DashboardUiData(

    val widgets: List<MonitorWidgetUiState>,

    val lastUpdatedEpochMs: Long,

)



private const val CHART_WINDOW = 60



@HiltViewModel

class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val observeCpuUsageUseCase: ObserveCpuUsageUseCase,

    private val observeCpuArchitectureUseCase: ObserveCpuArchitectureUseCase,

    private val observeMemoryUsageUseCase: ObserveMemoryUsageUseCase,

    private val observeThermalUseCase: ObserveThermalUseCase,

    private val observeBatteryStatusUseCase: ObserveBatteryStatusUseCase,

    private val observeDeviceHealthUseCase: ObserveDeviceHealthUseCase,

) : BaseViewModel() {



    private val intervalMs = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS



    private var cpuHistory = emptyChartSeries(SERIES_CPU, MonitorColors.CpuUsage)

    private var memoryHistory = emptyChartSeries(SERIES_MEMORY, MonitorColors.MemoryUsage)

    private var thermalHistory = emptyChartSeries(SERIES_THERMAL, MonitorColors.Thermal)

    private var batteryHistory = emptyChartSeries(SERIES_BATTERY, MonitorColors.BatteryLevel)



    private val _uiState = MutableStateFlow<UiState<DashboardUiData>>(UiState.Loading)

    val uiState: StateFlow<UiState<DashboardUiData>> = _uiState.asStateFlow()



    init {

        startRealtimeMonitoring()

    }



    private fun startRealtimeMonitoring() {

        viewModelScope.launch {

            combine(

                combine(

                    observeCpuUsageUseCase(ObserveCpuUsageParams(intervalMs)),

                    observeMemoryUsageUseCase(ObserveMemoryUsageParams(intervalMs)),

                    observeThermalUseCase(ObserveThermalParams(intervalMs)),

                    observeBatteryStatusUseCase(ObserveBatteryStatusParams(intervalMs)),

                    observeCpuArchitectureUseCase(),

                ) { cpuResult, memoryResult, thermalResult, batteryResult, archResult ->

                    CoreMetricsSnapshot(cpuResult, memoryResult, thermalResult, batteryResult, archResult)

                },

                observeDeviceHealthUseCase(ObserveDeviceHealthParams(intervalMs)),

            ) { coreMetrics, healthResult ->

                DashboardSnapshot(

                    cpu = coreMetrics.cpu,

                    memory = coreMetrics.memory,

                    thermal = coreMetrics.thermal,

                    battery = coreMetrics.battery,

                    architecture = coreMetrics.architecture,

                    health = healthResult,

                )

            }.collect { snapshot ->

                val error = snapshot.firstError()

                if (error != null) {

                    _uiState.value = UiState.Error(error)

                    return@collect

                }



                val cpu = (snapshot.cpu as Result.Success).data

                val memory = (snapshot.memory as Result.Success).data

                val thermal = (snapshot.thermal as Result.Success).data

                val battery = (snapshot.battery as Result.Success).data

                val architecture = (snapshot.architecture as? Result.Success)?.data

                val health = (snapshot.health as Result.Success).data



                cpuHistory = cpuHistory.appendSample(cpu.totalUsagePercent)

                memoryHistory = memoryHistory.appendSample(toUsedPercent(memory))

                thermalHistory = thermalHistory.appendSample(thermal.cpuTemperatureCelsius)

                batteryHistory = batteryHistory.appendSample(battery.percentage.toFloat())



                _uiState.value = UiState.Success(

                    DashboardUiData(

                        widgets = buildWidgets(cpu, memory, thermal, battery, architecture, health),

                        lastUpdatedEpochMs = System.currentTimeMillis(),

                    ),

                )

            }

        }

    }



    private fun buildWidgets(

        cpu: CpuUsageMetrics,

        memory: MemoryMetrics,

        thermal: ThermalMetrics,

        battery: BatteryMetrics,

        architecture: CpuArchitectureInfo?,

        health: DeviceHealthReport,

    ): List<MonitorWidgetUiState> {

        val frequency = averageFrequencyMhz(architecture)



        return listOf(

            MonitorWidgetUiState(

                id = WIDGET_CPU,

                title = context.getString(R.string.widget_cpu_usage),

                subtitle = context.getString(R.string.subtitle_realtime),

                primaryMetric = MetricValueUiState(

                    label = context.getString(R.string.label_total),

                    value = cpu.totalUsagePercent.toPercentDisplay(),

                    unit = "%",

                    accentColor = MonitorColors.CpuUsage,

                ),

                chartSeries = cpuHistory,

            ),

            MonitorWidgetUiState(

                id = WIDGET_MEMORY,

                title = context.getString(R.string.widget_memory),

                subtitle = context.getString(R.string.subtitle_ram),

                primaryMetric = MetricValueUiState(

                    label = context.getString(R.string.label_used),

                    value = toUsedPercent(memory).toPercentDisplay(),

                    unit = "%",

                    accentColor = MonitorColors.MemoryUsage,

                ),

                chartSeries = memoryHistory,

            ),

            MonitorWidgetUiState(

                id = WIDGET_THERMAL,

                title = context.getString(R.string.widget_cpu_temp),

                subtitle = context.getString(R.string.subtitle_thermal),

                primaryMetric = MetricValueUiState(

                    label = context.getString(R.string.label_current),

                    value = thermal.cpuTemperatureCelsius.toPercentDisplay(0),

                    unit = "°C",

                    accentColor = MonitorColors.Thermal,

                ),

                chartSeries = thermalHistory,

            ),

            MonitorWidgetUiState(

                id = WIDGET_FREQUENCY,

                title = context.getString(R.string.widget_cpu_freq),

                subtitle = context.getString(R.string.subtitle_clock),

                primaryMetric = MetricValueUiState(

                    label = context.getString(R.string.label_avg),

                    value = frequency.toFrequencyMhzDisplay(),

                    unit = "MHz",

                    accentColor = MonitorColors.CpuFrequency,

                ),

                chartSeries = null,

            ),

            MonitorWidgetUiState(

                id = WIDGET_BATTERY,

                title = context.getString(R.string.widget_battery),

                subtitle = if (battery.isCharging) {
                    context.getString(R.string.subtitle_charging)
                } else {
                    context.getString(R.string.subtitle_discharging)
                },

                primaryMetric = MetricValueUiState(

                    label = context.getString(R.string.label_level),

                    value = battery.percentage.toString(),

                    unit = "%",

                    accentColor = MonitorColors.BatteryLevel,

                ),

                chartSeries = batteryHistory,

            ),

            MonitorWidgetUiState(

                id = WIDGET_HEALTH,

                title = context.getString(R.string.widget_device_health),

                subtitle = healthStatusLabel(health.status),

                primaryMetric = MetricValueUiState(

                    label = context.getString(R.string.label_score),

                    value = health.overallScore.toString(),

                    unit = "/100",

                    accentColor = MonitorColors.HealthScore,

                ),

                chartSeries = null,

            ),

        )

    }



    private fun toUsedPercent(memory: MemoryMetrics): Float =

        if (memory.totalBytes <= 0L) {

            0f

        } else {

            ((memory.usedBytes.toDouble() / memory.totalBytes.toDouble()) * 100.0)

                .roundToInt()

                .toFloat()

        }



    private fun healthStatusLabel(status: HealthStatus): String = when (status) {
        HealthStatus.EXCELLENT -> context.getString(R.string.health_excellent)
        HealthStatus.GOOD -> context.getString(R.string.health_good)
        HealthStatus.FAIR -> context.getString(R.string.health_fair)
        HealthStatus.POOR -> context.getString(R.string.health_poor)
        HealthStatus.CRITICAL -> context.getString(R.string.health_critical)
    }

    private fun averageFrequencyMhz(architecture: CpuArchitectureInfo?): Float =

        architecture?.processors

            ?.mapNotNull { it.currentFrequencyMhz }

            ?.average()

            ?.toFloat()

            ?: 0f



    private fun emptyChartSeries(id: String, color: androidx.compose.ui.graphics.Color): ChartSeriesUiState =

        ChartSeriesUiState(

            id = id,

            label = id,

            values = emptyList(),

            maxPoints = CHART_WINDOW,

            lineColor = color,

        )



    private data class CoreMetricsSnapshot(

        val cpu: Result<CpuUsageMetrics>,

        val memory: Result<MemoryMetrics>,

        val thermal: Result<ThermalMetrics>,

        val battery: Result<BatteryMetrics>,

        val architecture: Result<CpuArchitectureInfo>,

    )



    private data class DashboardSnapshot(

        val cpu: Result<CpuUsageMetrics>,

        val memory: Result<MemoryMetrics>,

        val thermal: Result<ThermalMetrics>,

        val battery: Result<BatteryMetrics>,

        val architecture: Result<CpuArchitectureInfo>,

        val health: Result<DeviceHealthReport>,

    ) {

        fun firstError(): String? {

            val results = listOf(cpu, memory, thermal, battery, architecture, health)

            return results

                .filterIsInstance<Result.Error>()

                .firstOrNull()

                ?.exception

                ?.message

        }

    }



    private companion object {

        const val WIDGET_CPU = "widget_cpu"

        const val WIDGET_MEMORY = "widget_memory"

        const val WIDGET_THERMAL = "widget_thermal"

        const val WIDGET_FREQUENCY = "widget_frequency"

        const val WIDGET_BATTERY = "widget_battery"

        const val WIDGET_HEALTH = "widget_health"

        const val SERIES_CPU = "series_cpu"

        const val SERIES_MEMORY = "series_memory"

        const val SERIES_THERMAL = "series_thermal"

        const val SERIES_BATTERY = "series_battery"

    }

}


