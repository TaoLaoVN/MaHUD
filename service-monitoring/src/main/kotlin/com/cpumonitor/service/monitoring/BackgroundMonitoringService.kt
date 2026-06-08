package com.cpumonitor.service.monitoring

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.cpumonitor.core.logging.Logger
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.settings.AppSettings
import com.cpumonitor.domain.repository.SettingsRepository
import com.cpumonitor.domain.usecase.alert.MonitorAlertsParams
import com.cpumonitor.domain.usecase.alert.MonitorAlertsUseCase
import com.cpumonitor.domain.usecase.monitoring.PersistPolledMetricsParams
import com.cpumonitor.domain.usecase.monitoring.PersistPolledMetricsUseCase
import com.cpumonitor.service.monitoring.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that polls system metrics and persists them for history analytics.
 */
@AndroidEntryPoint
class BackgroundMonitoringService : LifecycleService() {

    @Inject
    lateinit var persistPolledMetricsUseCase: PersistPolledMetricsUseCase

    @Inject
    lateinit var monitorAlertsUseCase: MonitorAlertsUseCase

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private var metricsJob: Job? = null
    private var alertsJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            MonitoringServiceContract.ACTION_STOP -> {
                stopBackgroundMonitoring()
                return START_NOT_STICKY
            }

            MonitoringServiceContract.ACTION_START, null -> startBackgroundMonitoring()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        stopMetricsCollection()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Logger.i("Task removed — keeping background monitoring alive")
        MonitoringServiceLauncher.start(applicationContext)
    }

    private fun startBackgroundMonitoring() {
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            },
        )
        startMetricsCollection()
    }

    private fun startMetricsCollection() {
        metricsJob?.cancel()
        metricsJob = lifecycleScope.launch {
            settingsRepository.observeAppSettings()
                .map { settings -> settings.toPollIntervalMs() }
                .distinctUntilChangedBy { it }
                .collectLatest { intervalMs ->
                    collectAndPersistMetrics(intervalMs)
                }
        }

        alertsJob?.cancel()
        alertsJob = lifecycleScope.launch {
            settingsRepository.observeAppSettings()
                .map { settings -> settings.toPollIntervalMs() }
                .distinctUntilChangedBy { it }
                .collectLatest { intervalMs ->
                    monitorAlertsUseCase(MonitorAlertsParams(intervalMs = intervalMs))
                        .collect { result ->
                            when (result) {
                                is Result.Success -> Unit
                                is Result.Error -> Logger.e(
                                    "Alert monitoring failed: ${result.exception.message}",
                                    result.exception,
                                )
                                Result.Loading -> Unit
                            }
                        }
                }
        }
    }

    private suspend fun collectAndPersistMetrics(intervalMs: Long) {
        persistPolledMetricsUseCase(PersistPolledMetricsParams(intervalMs = intervalMs))
            .collect { result ->
                when (result) {
                    is Result.Success -> Unit
                    is Result.Error -> Logger.e(
                        "Metric persistence failed: ${result.exception.message}",
                        result.exception,
                    )
                    Result.Loading -> Unit
                }
            }
    }

    private fun stopMetricsCollection() {
        metricsJob?.cancel()
        metricsJob = null
        alertsJob?.cancel()
        alertsJob = null
    }

    private fun stopBackgroundMonitoring() {
        stopMetricsCollection()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.monitoring_notification_channel_name),
            NotificationManager.IMPORTANCE_MIN,
        ).apply {
            description = getString(R.string.monitoring_notification_channel_description)
            setShowBadge(false)
            enableVibration(false)
            setSound(null, null)
        }

        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, BackgroundMonitoringService::class.java).apply {
            action = MonitoringServiceContract.ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            STOP_REQUEST_CODE,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.monitoring_notification_title))
            .setContentText(getString(R.string.monitoring_notification_text))
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(android.R.string.cancel),
                stopPendingIntent,
            )
            .build()
    }

    private fun AppSettings.toPollIntervalMs(): Long =
        backgroundRefreshIntervalSeconds
            .coerceIn(
                AppSettings.MIN_BACKGROUND_REFRESH_SECONDS,
                AppSettings.MAX_BACKGROUND_REFRESH_SECONDS,
            )
            .toLong() * 1_000L

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "background_monitor_channel"
        private const val NOTIFICATION_ID = 6001
        private const val STOP_REQUEST_CODE = 6002
    }
}
