package com.cpumonitor.domain.usecase.monitoring

import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.StorageMetrics
import com.cpumonitor.domain.repository.MonitoringConstants
import com.cpumonitor.domain.repository.StorageRepository
import com.cpumonitor.domain.usecase.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class ObserveStorageUsageParams(
    val intervalMs: Long = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS,
)

/**
 * Streams realtime internal storage usage from the repository.
 */
class ObserveStorageUsageUseCase @Inject constructor(
    private val storageRepository: StorageRepository,
) : FlowUseCase<ObserveStorageUsageParams, StorageMetrics>() {

    override fun execute(params: ObserveStorageUsageParams): Flow<StorageMetrics> =
        storageRepository.observeStorageUsage(params.intervalMs).map(::unwrap)
}
