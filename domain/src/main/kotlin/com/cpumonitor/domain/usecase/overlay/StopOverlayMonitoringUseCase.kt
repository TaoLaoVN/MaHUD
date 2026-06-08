package com.cpumonitor.domain.usecase.overlay

import com.cpumonitor.domain.gateway.OverlayServiceController
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.usecase.NoParamsUseCase
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
 * Stops the overlay foreground service and removes the floating window.
 */
class StopOverlayMonitoringUseCase @Inject constructor(
    private val overlayServiceController: OverlayServiceController,
) : NoParamsUseCase<Unit>(Dispatchers.Main.immediate) {

    override suspend fun execute(): Result<Unit> {
        return try {
            overlayServiceController.stopMonitoring()
            Result.Success(Unit)
        } catch (exception: Exception) {
            Result.Error(
                com.cpumonitor.domain.model.DomainException(
                    message = exception.message ?: "Failed to stop overlay service",
                    cause = exception,
                ),
            )
        }
    }
}
