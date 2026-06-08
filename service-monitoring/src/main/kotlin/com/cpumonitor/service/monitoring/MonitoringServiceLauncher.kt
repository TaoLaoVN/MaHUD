package com.cpumonitor.service.monitoring

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

/**
 * Starts and stops the background metrics collection foreground service.
 */
object MonitoringServiceLauncher {

    fun start(context: Context) {
        val intent = Intent(context, BackgroundMonitoringService::class.java).apply {
            action = MonitoringServiceContract.ACTION_START
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun stop(context: Context) {
        val intent = Intent(context, BackgroundMonitoringService::class.java).apply {
            action = MonitoringServiceContract.ACTION_STOP
        }
        context.startService(intent)
    }
}
