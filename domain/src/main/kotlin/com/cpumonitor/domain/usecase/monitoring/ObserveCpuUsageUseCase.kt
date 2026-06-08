package com.cpumonitor.domain.usecase.monitoring

import com.cpumonitor.domain.model.CpuUsageMetrics
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.repository.CpuRepository
import com.cpumonitor.domain.repository.MonitoringConstants
import com.cpumonitor.domain.usecase.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class ObserveCpuUsageParams(
    val intervalMs: Long = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS,
)

/**
 * Streams realtime CPU usage from the repository.
 */
class ObserveCpuUsageUseCase @Inject constructor(
    private val cpuRepository: CpuRepository,
) : FlowUseCase<ObserveCpuUsageParams, CpuUsageMetrics>() {

    override fun execute(params: ObserveCpuUsageParams): Flow<CpuUsageMetrics> =
        cpuRepository.observeCpuUsage(params.intervalMs).map(::unwrap)
}
