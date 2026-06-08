package com.cpumonitor.domain.usecase.overlay

import com.cpumonitor.domain.model.OverlayMetrics
import com.cpumonitor.domain.repository.OverlayRepository
import com.cpumonitor.domain.usecase.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class ObserveOverlayMetricsParams(val refreshIntervalMs: Long)

/**
 * Streams overlay metrics from the repository for preview or floating window display.
 */
class ObserveOverlayMetricsUseCase @Inject constructor(
    private val overlayRepository: OverlayRepository,
) : FlowUseCase<ObserveOverlayMetricsParams, OverlayMetrics>() {

    override fun execute(params: ObserveOverlayMetricsParams): Flow<OverlayMetrics> =
        overlayRepository.observeMetrics(params.refreshIntervalMs)
}
