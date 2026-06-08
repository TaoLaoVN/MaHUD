package com.cpumonitor.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cpumonitor.feature.settings.ui.SettingsScreen

const val SettingsRoute = "settings"

fun NavGraphBuilder.settingsScreen() {
    composable(route = SettingsRoute) {
        SettingsScreen()
    }
}
