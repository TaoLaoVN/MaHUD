package com.cpumonitor.feature.memory.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cpumonitor.feature.memory.ui.MemoryScreen

const val MemoryRoute = "memory"

fun NavGraphBuilder.memoryScreen() {
    composable(route = MemoryRoute) {
        MemoryScreen()
    }
}
