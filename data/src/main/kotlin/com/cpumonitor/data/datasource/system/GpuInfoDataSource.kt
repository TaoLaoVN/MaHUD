package com.cpumonitor.data.datasource.system

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ConfigurationInfo
import com.cpumonitor.data.datasource.SystemDataSource
import com.cpumonitor.domain.model.device.GpuInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads GPU metadata from GLES configuration and common sysfs paths.
 */
@Singleton
class GpuInfoDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : SystemDataSource {

    fun readGpuInfo(): GpuInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo: ConfigurationInfo = activityManager.deviceConfigurationInfo
        val openGlEsVersion = configurationInfo.glEsVersion.takeIf { it.isNotBlank() }

        val sysfs = readSysfsGpuInfo()
        return GpuInfo(
            renderer = sysfs.renderer,
            vendor = sysfs.vendor,
            openGlEsVersion = openGlEsVersion,
        )
    }

    private fun readSysfsGpuInfo(): SysfsGpuInfo {
        val rendererCandidates = listOf(
            "/sys/class/kgsl/kgsl-3d0/gpu_model",
            "/sys/kernel/gpu/gpu_model",
            "/sys/devices/platform/mali.0/uevent",
            "/sys/class/misc/mali0/device/uevent",
        )
        val vendorCandidates = listOf(
            "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage",
            "/sys/kernel/gpu/gpu_busy",
        )

        val renderer = rendererCandidates.firstNotNullOfOrNull(::readFirstLine)
        val vendor = vendorCandidates.firstNotNullOfOrNull(::readFirstLine)
            ?: renderer?.substringBefore(' ')?.takeIf { it.isNotBlank() }

        return SysfsGpuInfo(renderer = renderer, vendor = vendor)
    }

    private fun readFirstLine(path: String): String? {
        val file = File(path)
        if (!file.canRead()) return null
        return file.readLines().firstOrNull { line ->
            val trimmed = line.trim()
            trimmed.isNotEmpty() && !trimmed.startsWith("DEVTYPE=")
        }?.let { line ->
            if (line.contains('=')) {
                line.substringAfter('=').trim()
            } else {
                line.trim()
            }
        }
    }

    private data class SysfsGpuInfo(
        val renderer: String?,
        val vendor: String?,
    )
}
