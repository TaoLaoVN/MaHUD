package com.cpumonitor.domain.usecase.analytics

import com.cpumonitor.domain.model.analytics.AnalyticsDashboard
import com.cpumonitor.domain.model.analytics.DeviceHealthReport
import com.cpumonitor.domain.model.history.HistoryTimeRange
import com.cpumonitor.domain.repository.AnalyticsRepository
import com.cpumonitor.domain.usecase.FlowUseCase
import com.cpumonitor.domain.usecase.UseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class ObserveAnalyticsDashboardParams(
    val intervalMs: Long,
    val window: HistoryTimeRange = HistoryTimeRange.TWENTY_FOUR_HOURS,
)

data class ObserveDeviceHealthParams(
    val intervalMs: Long,
    val window: HistoryTimeRange = HistoryTimeRange.TWENTY_FOUR_HOURS,
)

class ObserveAnalyticsDashboardUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
) : FlowUseCase<ObserveAnalyticsDashboardParams, AnalyticsDashboard>() {

    override fun execute(params: ObserveAnalyticsDashboardParams): Flow<AnalyticsDashboard> =
        analyticsRepository.observeAnalyticsDashboard(
            intervalMs = params.intervalMs,
            window = params.window,
        ).map { result ->
            when (result) {
                is com.cpumonitor.domain.model.Result.Success -> result.data
                is com.cpumonitor.domain.model.Result.Error -> throw result.exception
                com.cpumonitor.domain.model.Result.Loading -> throw IllegalStateException("Analytics loading")
            }
        }
}

class ObserveDeviceHealthUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
) : FlowUseCase<ObserveDeviceHealthParams, DeviceHealthReport>() {

    override fun execute(params: ObserveDeviceHealthParams): Flow<DeviceHealthReport> =
        analyticsRepository.observeDeviceHealth(
            intervalMs = params.intervalMs,
            window = params.window,
        ).map { result ->
            when (result) {
                is com.cpumonitor.domain.model.Result.Success -> result.data
                is com.cpumonitor.domain.model.Result.Error -> throw result.exception
                com.cpumonitor.domain.model.Result.Loading -> throw IllegalStateException("Health loading")
            }
        }
}

class GetAnalyticsDashboardUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
) : UseCase<HistoryTimeRange, AnalyticsDashboard>() {

    override suspend fun execute(params: HistoryTimeRange) =
        analyticsRepository.getAnalyticsDashboard(window = params)
}
