package com.cpumonitor.feature.cpu.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cpumonitor.feature.cpu.ui.CpuScreen

const val CpuRoute = "cpu"

fun NavGraphBuilder.cpuScreen() {
    composable(route = CpuRoute) {
        CpuScreen()
    }
}
