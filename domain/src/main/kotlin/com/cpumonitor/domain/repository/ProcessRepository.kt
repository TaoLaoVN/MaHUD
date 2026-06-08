package com.cpumonitor.domain.repository

import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.process.ProcessSortOrder
import com.cpumonitor.domain.model.process.RunningProcess
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for running process monitoring data.
 */
interface ProcessRepository : Repository {

    fun observeRunningProcesses(
        intervalMs: Long = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS,
        sortOrder: ProcessSortOrder = ProcessSortOrder.CPU,
    ): Flow<Result<List<RunningProcess>>>
}
