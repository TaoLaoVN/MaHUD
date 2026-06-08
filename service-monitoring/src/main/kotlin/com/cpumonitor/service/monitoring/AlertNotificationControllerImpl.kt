package com.cpumonitor.service.monitoring

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.cpumonitor.domain.gateway.AlertNotificationController
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertNotificationControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AlertNotificationController {

    init {
        createChannel()
    }

    override fun showAlert(title: String, message: String, notificationId: Int) {
        val notification = NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            ?.notify(notificationId, notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            ALERT_CHANNEL_ID,
            "Monitoring Alerts",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Threshold alerts for CPU, memory, thermal, and battery"
        }

        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    private companion object {
        const val ALERT_CHANNEL_ID = "monitoring_alerts_channel"
    }
}
