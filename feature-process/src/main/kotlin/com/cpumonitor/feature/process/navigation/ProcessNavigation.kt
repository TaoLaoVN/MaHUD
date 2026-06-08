package com.cpumonitor.feature.process.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cpumonitor.feature.process.ui.ProcessScreen

const val ProcessRoute = "process"

fun NavGraphBuilder.processScreen() {
    composable(route = ProcessRoute) {
        ProcessScreen()
    }
}
