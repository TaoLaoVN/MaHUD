package com.cpumonitor.pro

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.cpumonitor.core.logging.Logger
import com.cpumonitor.service.monitoring.MonitoringServiceLauncher
import com.cpumonitor.service.monitoring.worker.RetentionWorkScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CPUMonitorApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        Logger.init(isDebug = BuildConfig.DEBUG)
        Logger.i("CPUMonitorApplication started")

        RetentionWorkScheduler.schedule(this)
        MonitoringServiceLauncher.start(this)
    }
}
