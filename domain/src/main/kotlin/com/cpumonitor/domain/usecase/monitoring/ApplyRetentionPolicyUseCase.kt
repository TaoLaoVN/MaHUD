package com.cpumonitor.domain.usecase.monitoring

import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.settings.RetentionPeriod
import com.cpumonitor.domain.model.settings.RetentionPolicy
import com.cpumonitor.domain.repository.MetricsRepository
import com.cpumonitor.domain.repository.SettingsRepository
import com.cpumonitor.domain.usecase.NoParamsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Deletes historical metrics that fall outside the configured [RetentionPolicy] windows.
 */
class ApplyRetentionPolicyUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val metricsRepository: MetricsRepository,
) : NoParamsUseCase<Unit>(Dispatchers.IO) {

    override suspend fun execute(): Result<Unit> {
        val policy = settingsRepository.observeRetentionPolicy().first()
        val nowMillis = System.currentTimeMillis()

        val outcomes = listOf(
            applyRetention(policy.cpuRetention, nowMillis) { cutoff ->
                metricsRepository.deleteCpuMetricsBefore(cutoff)
            },
            applyRetention(policy.memoryRetention, nowMillis) { cutoff ->
                metricsRepository.deleteMemoryMetricsBefore(cutoff)
            },
            applyRetention(policy.thermalRetention, nowMillis) { cutoff ->
                metricsRepository.deleteThermalMetricsBefore(cutoff)
            },
            applyRetention(policy.batteryRetention, nowMillis) { cutoff ->
                metricsRepository.deleteBatteryMetricsBefore(cutoff)
            },
        )

        val firstError = outcomes.filterIsInstance<Result.Error>().firstOrNull()
        return firstError ?: Result.Success(Unit)
    }

    private suspend fun applyRetention(
        period: RetentionPeriod,
        nowMillis: Long,
        deleteBefore: suspend (Long) -> Result<Unit>,
    ): Result<Unit> {
        val retentionMillis = period.toRetentionMillis() ?: return Result.Success(Unit)
        val cutoffMillis = nowMillis - retentionMillis
        return deleteBefore(cutoffMillis)
    }
}
