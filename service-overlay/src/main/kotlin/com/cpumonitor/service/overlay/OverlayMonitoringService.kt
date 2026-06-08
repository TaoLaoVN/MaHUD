package com.cpumonitor.service.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.cpumonitor.core.logging.Logger
import com.cpumonitor.domain.model.OverlayConfig
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.usecase.overlay.ObserveOverlayMetricsParams
import com.cpumonitor.domain.usecase.overlay.ObserveOverlayMetricsUseCase
import com.cpumonitor.service.overlay.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that hosts the floating overlay and streams metrics via injected use cases.
 */
@AndroidEntryPoint
class OverlayMonitoringService : LifecycleService() {

    @Inject
    lateinit var observeOverlayMetricsUseCase: ObserveOverlayMetricsUseCase

    @Inject
    lateinit var overlayServiceController: OverlayServiceControllerImpl

    private var overlayWindowManager: OverlayWindowManager? = null
    private var metricsJob: Job? = null
    private var gamingModeEnabled = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        overlayWindowManager = OverlayWindowManager(
            context = this,
            lifecycleOwner = this,
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            OverlayServiceContract.ACTION_STOP -> {
                stopOverlayMonitoring()
                return START_NOT_STICKY
            }

            OverlayServiceContract.ACTION_START, null -> {
                gamingModeEnabled = intent?.getBooleanExtra(
                    OverlayServiceContract.EXTRA_GAMING_MODE,
                    false,
                ) ?: false
                startOverlayMonitoring()
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        stopOverlayMonitoring()
        super.onDestroy()
    }

    private fun startOverlayMonitoring() {
        try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                buildNotification(),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    0
                },
            )
        } catch (exception: Exception) {
            Logger.e("Failed to start overlay foreground service", exception)
            stopSelf()
            return
        }

        if (!Settings.canDrawOverlays(this)) {
            Logger.w("Overlay permission missing; foreground service running without floating window")
            return
        }

        try {
            overlayWindowManager?.show(gamingModeEnabled)
            startMetricsCollection()
        } catch (exception: Exception) {
            Logger.e("Failed to show overlay window", exception)
            stopOverlayMonitoring()
        }
    }

    private fun startMetricsCollection() {
        metricsJob?.cancel()
        val refreshIntervalMs = if (gamingModeEnabled) {
            OverlayConfig.GAMING_REFRESH_INTERVAL_MS
        } else {
            OverlayConfig.NORMAL_REFRESH_INTERVAL_MS
        }

        metricsJob = lifecycleScope.launch {
            observeOverlayMetricsUseCase(
                ObserveOverlayMetricsParams(refreshIntervalMs = refreshIntervalMs),
            ).collectLatest { result ->
                when (result) {
                    is Result.Success -> overlayWindowManager?.updateMetrics(result.data)
                    is Result.Error -> Logger.e(
                        "Overlay metrics stream failed: ${result.exception.message}",
                        result.exception,
                    )
                    Result.Loading -> Unit
                }
            }
        }
    }

    private fun stopOverlayMonitoring() {
        metricsJob?.cancel()
        metricsJob = null
        overlayWindowManager?.hide()
        overlayServiceController.markStopped()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.overlay_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.overlay_notification_channel_description)
            setShowBadge(false)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, OverlayMonitoringService::class.java).apply {
            action = OverlayServiceContract.ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            STOP_REQUEST_CODE,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.overlay_notification_title))
            .setContentText(getString(R.string.overlay_notification_text))
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(android.R.string.cancel),
                stopPendingIntent,
            )
            .build()
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "overlay_monitor_channel"
        private const val NOTIFICATION_ID = 7001
        private const val STOP_REQUEST_CODE = 7002
    }
}
