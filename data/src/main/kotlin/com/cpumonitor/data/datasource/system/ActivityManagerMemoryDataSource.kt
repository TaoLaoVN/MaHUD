package com.cpumonitor.data.datasource.system

import android.app.ActivityManager
import android.content.Context
import com.cpumonitor.data.datasource.SystemDataSource
import com.cpumonitor.domain.model.MemoryMetrics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads system memory metrics via [ActivityManager.getMemoryInfo].
 */
@Singleton
class ActivityManagerMemoryDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : SystemDataSource {

    private val activityManager: ActivityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    fun readMemoryMetrics(): MemoryMetrics {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalBytes = memoryInfo.totalMem
        val availableBytes = memoryInfo.availMem
        val usedBytes = (totalBytes - availableBytes).coerceAtLeast(0L)

        return MemoryMetrics(
            timestampMillis = System.currentTimeMillis(),
            totalBytes = totalBytes,
            usedBytes = usedBytes,
            availableBytes = availableBytes,
            freeBytes = availableBytes,
            cachedBytes = 0L,
            isLowMemory = memoryInfo.lowMemory,
            lowMemoryThresholdBytes = memoryInfo.threshold,
        )
    }
}
