package com.cpumonitor.data.repository

import com.cpumonitor.core.common.dispatcher.DispatchersProvider
import com.cpumonitor.data.datasource.system.StorageDataSource
import com.cpumonitor.data.util.monitoringPollFlow
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.StorageMetrics
import com.cpumonitor.domain.repository.MonitoringConstants
import com.cpumonitor.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation for internal storage monitoring metrics.
 */
@Singleton
class StorageRepositoryImpl @Inject constructor(
    private val storageDataSource: StorageDataSource,
    dispatchersProvider: DispatchersProvider,
) : BaseRepository(dispatchersProvider.io), StorageRepository {

    override fun observeStorageUsage(intervalMs: Long): Flow<Result<StorageMetrics>> =
        observeSafely {
            monitoringPollFlow(intervalMs.coerceAtLeast(MonitoringConstants.MIN_REFRESH_INTERVAL_MS)) {
                storageDataSource.readStorageMetrics()
            }
                .distinctUntilChangedBy { metrics ->
                    metrics.usedBytes to metrics.freeBytes
                }
                .flowOn(dispatcher)
        }

    override suspend fun getStorageUsage(): Result<StorageMetrics> =
        safeCall { storageDataSource.readStorageMetrics() }
}
