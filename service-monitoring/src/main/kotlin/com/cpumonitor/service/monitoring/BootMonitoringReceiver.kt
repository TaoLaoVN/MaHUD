package com.cpumonitor.service.monitoring

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cpumonitor.core.logging.Logger

/**
 * Restarts background metric collection after device reboot.
 */
class BootMonitoringReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        Logger.i("Boot completed — restarting background monitoring service")
        MonitoringServiceLauncher.start(context.applicationContext)
    }
}
