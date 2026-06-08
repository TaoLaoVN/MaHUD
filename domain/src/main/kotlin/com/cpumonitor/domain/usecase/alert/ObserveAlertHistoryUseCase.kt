package com.cpumonitor.domain.usecase.alert

import com.cpumonitor.domain.model.alert.AlertHistoryEntry
import com.cpumonitor.domain.repository.AlertRepository
import com.cpumonitor.domain.usecase.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class ObserveAlertHistoryParams(val limit: Int = 100)

class ObserveAlertHistoryUseCase @Inject constructor(
    private val alertRepository: AlertRepository,
) : FlowUseCase<ObserveAlertHistoryParams, List<AlertHistoryEntry>>() {

    override fun execute(params: ObserveAlertHistoryParams): Flow<List<AlertHistoryEntry>> =
        alertRepository.observeAlertHistory(params.limit)
}
