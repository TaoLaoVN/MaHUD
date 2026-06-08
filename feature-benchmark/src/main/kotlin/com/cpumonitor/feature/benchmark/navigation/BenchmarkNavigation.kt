package com.cpumonitor.feature.benchmark.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cpumonitor.feature.benchmark.ui.BenchmarkScreen

const val BenchmarkRoute = "benchmark"

fun NavGraphBuilder.benchmarkScreen() {
    composable(route = BenchmarkRoute) {
        BenchmarkScreen()
    }
}
