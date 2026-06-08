package com.cpumonitor.feature.battery.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cpumonitor.feature.battery.ui.BatteryScreen

const val BatteryRoute = "battery"

fun NavGraphBuilder.batteryScreen() {
    composable(route = BatteryRoute) {
        BatteryScreen()
    }
}
