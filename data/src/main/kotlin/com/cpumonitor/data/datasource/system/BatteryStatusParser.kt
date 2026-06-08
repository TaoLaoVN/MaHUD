package com.cpumonitor.data.datasource.system

import android.os.BatteryManager

/**
 * Pure parsing helpers for battery status values.
 */
internal object BatteryStatusParser {

    fun calculatePercentage(level: Int, scale: Int): Int =
        if (level >= 0 && scale > 0) {
            ((level.toFloat() / scale.toFloat()) * 100f).toInt().coerceIn(0, 100)
        } else {
            0
        }

    fun temperatureCelsius(temperatureTenthCelsius: Int): Float =
        temperatureTenthCelsius / 10f

    fun isCharging(status: Int): Boolean =
        status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

    fun mapHealth(healthCode: Int): String = when (healthCode) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
        BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
        else -> "Unknown"
    }

    fun currentMa(currentMicroAmps: Int, averageMicroAmps: Int): Int {
        if (currentMicroAmps != Int.MIN_VALUE && currentMicroAmps != 0) {
            return currentMicroAmps / 1_000
        }
        if (averageMicroAmps != Int.MIN_VALUE && averageMicroAmps != 0) {
            return averageMicroAmps / 1_000
        }
        return 0
    }

    fun chargeSpeedMw(voltageMv: Int, currentMa: Int, isCharging: Boolean): Int? =
        if (isCharging && voltageMv > 0 && currentMa != 0) {
            voltageMv * currentMa / 1_000
        } else {
            null
        }
}
