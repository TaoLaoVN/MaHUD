package com.cpumonitor.domain.usecase.monitoring

import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.ThermalMetrics
import com.cpumonitor.domain.repository.MonitoringConstants
import com.cpumonitor.domain.repository.ThermalRepository
import com.cpumonitor.domain.usecase.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class ObserveThermalParams(
    val intervalMs: Long = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS,
)

/**
 * Streams realtime thermal readings from the repository.
 */
class ObserveThermalUseCase @Inject constructor(
    private val thermalRepository: ThermalRepository,
) : FlowUseCase<ObserveThermalParams, ThermalMetrics>() {

    override fun execute(params: ObserveThermalParams): Flow<ThermalMetrics> =
        thermalRepository.observeThermal(params.intervalMs).map(::unwrap)
}
