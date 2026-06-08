package com.cpumonitor.data.repository

import com.cpumonitor.core.common.dispatcher.DispatchersProvider
import com.cpumonitor.data.datasource.system.ActivityManagerProcessDataSource
import com.cpumonitor.data.util.monitoringPollFlow
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.process.ProcessSortOrder
import com.cpumonitor.domain.model.process.RunningProcess
import com.cpumonitor.domain.repository.MonitoringConstants
import com.cpumonitor.domain.repository.ProcessRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcessRepositoryImpl @Inject constructor(
    private val activityManagerProcessDataSource: ActivityManagerProcessDataSource,
    dispatchersProvider: DispatchersProvider,
) : BaseRepository(dispatchersProvider.io), ProcessRepository {

    override fun observeRunningProcesses(
        intervalMs: Long,
        sortOrder: ProcessSortOrder,
    ): Flow<Result<List<RunningProcess>>> =
        observeSafely {
            monitoringPollFlow(intervalMs.coerceAtLeast(MonitoringConstants.MIN_REFRESH_INTERVAL_MS)) {
                activityManagerProcessDataSource.readRunningProcesses(sortOrder)
            }
                .distinctUntilChangedBy { processes -> processes.map { it.pid to it.memoryPssKb } }
                .flowOn(dispatcher)
        }
}
