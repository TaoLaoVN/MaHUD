package com.cpumonitor.data.datasource.system

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.cpumonitor.data.datasource.SystemDataSource
import com.cpumonitor.domain.model.BatteryMetrics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads full battery status from the sticky [Intent.ACTION_BATTERY_CHANGED] broadcast.
 */
@Singleton
class BatteryManagerDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : SystemDataSource {

    fun readBatteryMetrics(): BatteryMetrics {
        val batteryStatus = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
        )

        if (batteryStatus == null) {
            return emptyBatteryMetrics()
        }

        val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val percentage = BatteryStatusParser.calculatePercentage(level, scale)

        val voltageMv = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        val temperatureTenthCelsius = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val temperatureCelsius = BatteryStatusParser.temperatureCelsius(temperatureTenthCelsius)

        val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
        val isCharging = BatteryStatusParser.isCharging(status)

        val healthCode = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val health = BatteryStatusParser.mapHealth(healthCode)

        val currentMa = BatteryStatusParser.currentMa(
            currentMicroAmps = batteryStatus.getIntExtra(EXTRA_CURRENT_NOW, Int.MIN_VALUE),
            averageMicroAmps = batteryStatus.getIntExtra(EXTRA_CURRENT_AVERAGE, Int.MIN_VALUE),
        )
        val chargeSpeedMw = BatteryStatusParser.chargeSpeedMw(voltageMv, currentMa, isCharging)

        return BatteryMetrics(
            timestampMillis = System.currentTimeMillis(),
            percentage = percentage,
            voltageMv = voltageMv,
            currentMa = currentMa,
            temperatureCelsius = temperatureCelsius,
            health = health,
            isCharging = isCharging,
            chargeSpeedMw = chargeSpeedMw,
        )
    }

    private companion object {
        private const val EXTRA_CURRENT_NOW = "current_now"
        private const val EXTRA_CURRENT_AVERAGE = "current_average"
    }

    private fun emptyBatteryMetrics(): BatteryMetrics =
        BatteryMetrics(
            timestampMillis = System.currentTimeMillis(),
            percentage = 0,
            voltageMv = 0,
            currentMa = 0,
            temperatureCelsius = 0f,
            health = "Unknown",
            isCharging = false,
            chargeSpeedMw = null,
        )
}
