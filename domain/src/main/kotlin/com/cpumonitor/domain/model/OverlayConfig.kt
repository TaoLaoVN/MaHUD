package com.cpumonitor.domain.model

/**
 * Configuration for the overlay foreground service and floating window.
 */
data class OverlayConfig(
    val gamingModeEnabled: Boolean = false,
) {
    val refreshIntervalMs: Long
        get() = if (gamingModeEnabled) GAMING_REFRESH_INTERVAL_MS else NORMAL_REFRESH_INTERVAL_MS

    companion object {
        const val NORMAL_REFRESH_INTERVAL_MS = 1_000L
        const val GAMING_REFRESH_INTERVAL_MS = 2_000L
    }
}
