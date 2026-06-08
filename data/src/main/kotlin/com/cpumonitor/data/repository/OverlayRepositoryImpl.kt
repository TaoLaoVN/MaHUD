package com.cpumonitor.data.repository

import com.cpumonitor.core.common.dispatcher.DispatchersProvider
import com.cpumonitor.data.datasource.overlay.SystemOverlayMetricsDataSource
import com.cpumonitor.data.datasource.overlay.OverlayPermissionDataSource
import com.cpumonitor.domain.model.OverlayMetrics
import com.cpumonitor.domain.repository.OverlayRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OverlayRepositoryImpl @Inject constructor(
    dispatchersProvider: DispatchersProvider,
    private val permissionDataSource: OverlayPermissionDataSource,
    private val metricsDataSource: SystemOverlayMetricsDataSource,
) : BaseRepository(dispatchersProvider.io), OverlayRepository {

    override fun observeMetrics(refreshIntervalMs: Long): Flow<OverlayMetrics> =
        metricsDataSource.observe(refreshIntervalMs)

    override suspend fun isOverlayPermissionGranted(): Boolean =
        permissionDataSource.isOverlayPermissionGranted()
}
