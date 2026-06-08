package com.cpumonitor.domain.usecase.alert

import com.cpumonitor.domain.model.alert.AlertRule
import com.cpumonitor.domain.repository.AlertRepository
import com.cpumonitor.domain.usecase.NoParamsFlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAlertRulesUseCase @Inject constructor(
    private val alertRepository: AlertRepository,
) : NoParamsFlowUseCase<List<AlertRule>>() {

    override fun execute(): Flow<List<AlertRule>> =
        alertRepository.observeAlertRules()
}
