package com.cpumonitor.data.datasource.system

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import com.cpumonitor.data.datasource.SystemDataSource
import com.cpumonitor.domain.model.device.DeviceScreenInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * Reads display resolution, density, and refresh rate.
 */
@Singleton
class DisplayInfoDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : SystemDataSource {

    fun readScreenInfo(): DeviceScreenInfo {
        val display = resolveDefaultDisplay()
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        display.getRealMetrics(metrics)

        val refreshRateHz = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            display.mode?.refreshRate ?: display.refreshRate
        } else {
            display.refreshRate
        }

        val widthInches = metrics.xdpi.takeIf { it > 0f }?.let { metrics.widthPixels / it }
        val heightInches = metrics.ydpi.takeIf { it > 0f }?.let { metrics.heightPixels / it }

        return DeviceScreenInfo(
            widthPixels = metrics.widthPixels,
            heightPixels = metrics.heightPixels,
            densityDpi = metrics.densityDpi,
            density = metrics.density,
            refreshRateHz = refreshRateHz,
            physicalWidthInches = widthInches,
            physicalHeightInches = heightInches,
        )
    }

    fun estimateScreenDiagonalInches(screenInfo: DeviceScreenInfo): Float? {
        val width = screenInfo.physicalWidthInches ?: return null
        val height = screenInfo.physicalHeightInches ?: return null
        return sqrt((width * width + height * height).toDouble()).toFloat()
    }

    @Suppress("DEPRECATION")
    private fun resolveDefaultDisplay(): Display {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val displayManager = context.getSystemService(DisplayManager::class.java)
            return displayManager.getDisplay(Display.DEFAULT_DISPLAY)
        }
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return windowManager.defaultDisplay
    }
}
