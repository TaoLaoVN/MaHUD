package com.cpumonitor.domain.usecase.overlay

import com.cpumonitor.domain.gateway.OverlayServiceController
import com.cpumonitor.domain.model.DomainException
import com.cpumonitor.domain.model.OverlayConfig
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.repository.OverlayRepository
import com.cpumonitor.domain.usecase.UseCase
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
 * Validates overlay permission and starts the foreground monitoring service.
 */
class StartOverlayMonitoringUseCase @Inject constructor(
    private val overlayRepository: OverlayRepository,
    private val overlayServiceController: OverlayServiceController,
) : UseCase<OverlayConfig, Unit>(Dispatchers.Main.immediate) {

    override suspend fun execute(params: OverlayConfig): Result<Unit> {
        if (!overlayRepository.isOverlayPermissionGranted()) {
            return Result.Error(
                DomainException("Overlay permission is required. Grant SYSTEM_ALERT_WINDOW in settings."),
            )
        }
        return try {
            overlayServiceController.startMonitoring(params)
            Result.Success(Unit)
        } catch (exception: Exception) {
            Result.Error(
                DomainException(
                    message = exception.message ?: "Failed to start overlay service",
                    cause = exception,
                ),
            )
        }
    }
}
