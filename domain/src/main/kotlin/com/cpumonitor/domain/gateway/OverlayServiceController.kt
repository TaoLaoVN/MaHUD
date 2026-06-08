package com.cpumonitor.domain.gateway

import com.cpumonitor.domain.model.OverlayConfig

/**
 * Gateway for controlling the overlay foreground service without exposing Android APIs to domain callers.
 */
interface OverlayServiceController {
    fun startMonitoring(config: OverlayConfig)
    fun stopMonitoring()
    fun isMonitoringActive(): Boolean
}
