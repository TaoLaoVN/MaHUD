package com.cpumonitor.service.monitoring.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cpumonitor.domain.usecase.monitoring.ApplyRetentionPolicyUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Periodic WorkManager job that enforces configured metric retention windows.
 */
@HiltWorker
class MetricRetentionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val applyRetentionPolicyUseCase: ApplyRetentionPolicyUseCase,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): androidx.work.ListenableWorker.Result =
        when (val outcome = applyRetentionPolicyUseCase()) {
            is com.cpumonitor.domain.model.Result.Success -> androidx.work.ListenableWorker.Result.success()
            is com.cpumonitor.domain.model.Result.Error -> {
                if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                    androidx.work.ListenableWorker.Result.retry()
                } else {
                    androidx.work.ListenableWorker.Result.failure()
                }
            }
            com.cpumonitor.domain.model.Result.Loading -> androidx.work.ListenableWorker.Result.failure()
        }

    companion object {
        const val WORK_NAME = "metric_retention_cleanup"
        private const val MAX_RETRY_ATTEMPTS = 3
    }
}
