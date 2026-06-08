package com.cpumonitor.feature.analytics.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cpumonitor.feature.analytics.ui.AnalyticsScreen

const val AnalyticsRoute = "analytics"

fun NavGraphBuilder.analyticsScreen() {
    composable(route = AnalyticsRoute) {
        AnalyticsScreen()
    }
}
