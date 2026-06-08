package com.cpumonitor.feature.settings.ui

import com.cpumonitor.domain.model.update.AppRelease

enum class AppUpdatePhase {
    IDLE,
    CHECKING,
    UP_TO_DATE,
    UPDATE_AVAILABLE,
    DOWNLOADING,
    READY_TO_INSTALL,
    ERROR,
}

data class AppUpdateUiState(
    val phase: AppUpdatePhase = AppUpdatePhase.IDLE,
    val currentVersionName: String = "",
    val release: AppRelease? = null,
    val downloadProgress: Float = 0f,
    val message: String? = null,
)
