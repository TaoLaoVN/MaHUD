package com.cpumonitor.domain.usecase.settings

import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.settings.AppSettings
import com.cpumonitor.domain.model.settings.AppTheme
import com.cpumonitor.domain.model.settings.RetentionPolicy
import com.cpumonitor.domain.repository.SettingsRepository
import com.cpumonitor.domain.usecase.NoParamsFlowUseCase
import com.cpumonitor.domain.usecase.UseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAppSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : NoParamsFlowUseCase<AppSettings>() {

    override fun execute(): Flow<AppSettings> = settingsRepository.observeAppSettings()
}

class UpdateThemeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : UseCase<AppTheme, Unit>() {

    override suspend fun execute(params: AppTheme): Result<Unit> =
        settingsRepository.updateTheme(params)
}

class UpdateBackgroundRefreshIntervalUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : UseCase<Int, Unit>() {

    override suspend fun execute(params: Int): Result<Unit> =
        settingsRepository.updateBackgroundRefreshInterval(params)
}

class UpdateRetentionPolicyUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : UseCase<RetentionPolicy, Unit>() {

    override suspend fun execute(params: RetentionPolicy): Result<Unit> =
        settingsRepository.updateRetentionPolicy(params)
}
