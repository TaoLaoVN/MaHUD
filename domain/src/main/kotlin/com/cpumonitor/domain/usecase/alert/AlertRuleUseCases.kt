package com.cpumonitor.domain.usecase.alert

import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.alert.AlertRule
import com.cpumonitor.domain.repository.AlertRepository
import com.cpumonitor.domain.usecase.UseCase
import javax.inject.Inject

class SaveAlertRuleUseCase @Inject constructor(
    private val alertRepository: AlertRepository,
) : UseCase<AlertRule, Unit>() {

    override suspend fun execute(params: AlertRule): Result<Unit> =
        alertRepository.saveAlertRule(params)
}

class ToggleAlertRuleUseCase @Inject constructor(
    private val alertRepository: AlertRepository,
) : UseCase<AlertRule, Unit>() {

    override suspend fun execute(params: AlertRule): Result<Unit> =
        alertRepository.saveAlertRule(params.copy(enabled = !params.enabled))
}

class EnsureDefaultAlertRulesUseCase @Inject constructor(
    private val alertRepository: AlertRepository,
) : UseCase<Unit, Unit>() {

    override suspend fun execute(params: Unit): Result<Unit> =
        alertRepository.ensureDefaultRules()
}
