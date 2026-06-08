package com.cpumonitor.data.datasource.system

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.cpumonitor.data.datasource.SystemDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads battery temperature from the sticky [Intent.ACTION_BATTERY_CHANGED] broadcast.
 */
@Singleton
class BatteryTemperatureDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : SystemDataSource {

    fun readBatteryTemperatureCelsius(): Float {
        val batteryStatus = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
        ) ?: return 0f

        val temperatureTenthCelsius = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        return temperatureTenthCelsius / 10f
    }
}
