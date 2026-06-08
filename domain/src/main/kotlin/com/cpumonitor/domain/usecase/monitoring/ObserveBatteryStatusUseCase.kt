package com.cpumonitor.domain.usecase.monitoring

import com.cpumonitor.domain.model.BatteryMetrics
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.repository.BatteryRepository
import com.cpumonitor.domain.repository.MonitoringConstants
import com.cpumonitor.domain.usecase.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class ObserveBatteryStatusParams(
    val intervalMs: Long = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS,
)

/**
 * Streams realtime battery status from the repository.
 */
class ObserveBatteryStatusUseCase @Inject constructor(
    private val batteryRepository: BatteryRepository,
) : FlowUseCase<ObserveBatteryStatusParams, BatteryMetrics>() {

    override fun execute(params: ObserveBatteryStatusParams): Flow<BatteryMetrics> =
        batteryRepository.observeBatteryStatus(params.intervalMs).map(::unwrap)
}
