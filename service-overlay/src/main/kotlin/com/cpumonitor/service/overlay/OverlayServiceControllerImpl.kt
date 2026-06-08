package com.cpumonitor.service.overlay

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.cpumonitor.domain.gateway.OverlayServiceController
import com.cpumonitor.domain.model.OverlayConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OverlayServiceControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : OverlayServiceController {

    private val monitoringActive = AtomicBoolean(false)

    override fun startMonitoring(config: OverlayConfig) {
        val intent = Intent(context, OverlayMonitoringService::class.java).apply {
            action = OverlayServiceContract.ACTION_START
            putExtra(OverlayServiceContract.EXTRA_GAMING_MODE, config.gamingModeEnabled)
        }
        ContextCompat.startForegroundService(context, intent)
        monitoringActive.set(true)
    }

    override fun stopMonitoring() {
        val intent = Intent(context, OverlayMonitoringService::class.java).apply {
            action = OverlayServiceContract.ACTION_STOP
        }
        context.startService(intent)
        monitoringActive.set(false)
    }

    override fun isMonitoringActive(): Boolean = monitoringActive.get()

    internal fun markStopped() {
        monitoringActive.set(false)
    }
}
