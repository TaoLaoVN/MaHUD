package com.cpumonitor.data.datasource.system

import android.os.BatteryManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryStatusParserTest {

    @Test
    fun calculatePercentage_returnsExpectedValue() {
        assertEquals(50, BatteryStatusParser.calculatePercentage(level = 50, scale = 100))
        assertEquals(0, BatteryStatusParser.calculatePercentage(level = -1, scale = 100))
    }

    @Test
    fun temperatureCelsius_convertsTenths() {
        assertEquals(36.5f, BatteryStatusParser.temperatureCelsius(365), 0.01f)
    }

    @Test
    fun isCharging_detectsChargingStates() {
        assertTrue(BatteryStatusParser.isCharging(BatteryManager.BATTERY_STATUS_CHARGING))
        assertTrue(BatteryStatusParser.isCharging(BatteryManager.BATTERY_STATUS_FULL))
        assertFalse(BatteryStatusParser.isCharging(BatteryManager.BATTERY_STATUS_DISCHARGING))
    }

    @Test
    fun currentMa_prefersInstantReading() {
        assertEquals(1_500, BatteryStatusParser.currentMa(currentMicroAmps = 1_500_000, averageMicroAmps = 0))
        assertEquals(900, BatteryStatusParser.currentMa(currentMicroAmps = 0, averageMicroAmps = 900_000))
    }

    @Test
    fun chargeSpeedMw_returnsNullWhenNotCharging() {
        assertNull(BatteryStatusParser.chargeSpeedMw(voltageMv = 4_200, currentMa = 1_000, isCharging = false))
        assertEquals(4_200, BatteryStatusParser.chargeSpeedMw(voltageMv = 4_200, currentMa = 1_000, isCharging = true))
    }
}
