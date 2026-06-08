package com.cpumonitor.service.overlay

/**
 * Public contract for starting and stopping [OverlayMonitoringService].
 */
object OverlayServiceContract {
    const val ACTION_START = "com.cpumonitor.service.overlay.action.START"
    const val ACTION_STOP = "com.cpumonitor.service.overlay.action.STOP"
    const val EXTRA_GAMING_MODE = "extra_gaming_mode"
}
