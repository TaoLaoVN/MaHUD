package com.cpumonitor.feature.history.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cpumonitor.feature.history.ui.HistoryScreen

const val HistoryRoute = "history"

fun NavGraphBuilder.historyScreen() {
    composable(route = HistoryRoute) {
        HistoryScreen()
    }
}
