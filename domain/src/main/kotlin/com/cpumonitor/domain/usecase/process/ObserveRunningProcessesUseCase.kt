package com.cpumonitor.domain.usecase.process

import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.process.ProcessSortOrder
import com.cpumonitor.domain.model.process.RunningProcess
import com.cpumonitor.domain.repository.MonitoringConstants
import com.cpumonitor.domain.repository.ProcessRepository
import com.cpumonitor.domain.usecase.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class ObserveRunningProcessesParams(
    val intervalMs: Long = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS,
    val sortOrder: ProcessSortOrder = ProcessSortOrder.CPU,
)

class ObserveRunningProcessesUseCase @Inject constructor(
    private val processRepository: ProcessRepository,
) : FlowUseCase<ObserveRunningProcessesParams, List<RunningProcess>>() {

    override fun execute(params: ObserveRunningProcessesParams): Flow<List<RunningProcess>> =
        processRepository.observeRunningProcesses(params.intervalMs, params.sortOrder)
            .map { result ->
                when (result) {
                    is Result.Success -> result.data
                    is Result.Error -> throw result.exception
                    Result.Loading -> error("Unexpected loading state")
                }
            }
}
