package com.cpumonitor.feature.thermal.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cpumonitor.feature.thermal.ui.ThermalScreen

const val ThermalRoute = "thermal"

fun NavGraphBuilder.thermalScreen() {
    composable(route = ThermalRoute) {
        ThermalScreen()
    }
}
