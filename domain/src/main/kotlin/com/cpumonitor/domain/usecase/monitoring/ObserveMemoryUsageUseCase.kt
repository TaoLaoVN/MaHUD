package com.cpumonitor.domain.usecase.monitoring

import com.cpumonitor.domain.model.MemoryMetrics
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.repository.MemoryRepository
import com.cpumonitor.domain.repository.MonitoringConstants
import com.cpumonitor.domain.usecase.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class ObserveMemoryUsageParams(
    val intervalMs: Long = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS,
)

/**
 * Streams realtime system memory usage from the repository.
 */
class ObserveMemoryUsageUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository,
) : FlowUseCase<ObserveMemoryUsageParams, MemoryMetrics>() {

    override fun execute(params: ObserveMemoryUsageParams): Flow<MemoryMetrics> =
        memoryRepository.observeMemoryUsage(params.intervalMs).map(::unwrap)
}
