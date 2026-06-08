package com.cpumonitor.domain.usecase.overlay

import com.cpumonitor.domain.gateway.OverlayServiceController
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.usecase.NoParamsUseCase
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
 * Returns whether the overlay foreground service is currently active.
 */
class IsOverlayRunningUseCase @Inject constructor(
    private val overlayServiceController: OverlayServiceController,
) : NoParamsUseCase<Boolean>(Dispatchers.IO) {

    override suspend fun execute(): Result<Boolean> =
        Result.Success(overlayServiceController.isMonitoringActive())
}
