package com.cpumonitor.feature.export.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cpumonitor.feature.export.ui.ExportScreen

const val ExportRoute = "export"

fun NavGraphBuilder.exportScreen() {
    composable(route = ExportRoute) {
        ExportScreen()
    }
}
