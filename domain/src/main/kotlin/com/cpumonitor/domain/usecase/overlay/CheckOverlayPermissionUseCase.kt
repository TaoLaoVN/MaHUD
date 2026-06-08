package com.cpumonitor.domain.usecase.overlay

import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.repository.OverlayRepository
import com.cpumonitor.domain.usecase.NoParamsUseCase
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
 * Checks whether the app has been granted SYSTEM_ALERT_WINDOW (draw-overlays) permission.
 */
class CheckOverlayPermissionUseCase @Inject constructor(
    private val overlayRepository: OverlayRepository,
) : NoParamsUseCase<Boolean>(Dispatchers.IO) {

    override suspend fun execute(): Result<Boolean> =
        Result.Success(overlayRepository.isOverlayPermissionGranted())
}
