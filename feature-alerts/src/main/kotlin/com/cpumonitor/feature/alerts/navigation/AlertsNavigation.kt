package com.cpumonitor.feature.alerts.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cpumonitor.feature.alerts.ui.AlertsScreen

const val AlertsRoute = "alerts"

fun NavGraphBuilder.alertsScreen() {
    composable(route = AlertsRoute) {
        AlertsScreen()
    }
}
