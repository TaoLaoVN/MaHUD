package com.cpumonitor.feature.analytics.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.cpumonitor.core.ui.R
import com.cpumonitor.core.ui.localized.localizedDescription
import com.cpumonitor.core.ui.localized.localizedDisplayLine
import com.cpumonitor.core.ui.localized.localizedHealthComponentSummary
import com.cpumonitor.core.ui.localized.localizedName
import com.cpumonitor.core.ui.localized.localizedPerformanceSummary
import com.cpumonitor.core.ui.localized.localizedTitle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cpumonitor.core.designsystem.component.MonitorCard
import com.cpumonitor.core.designsystem.component.MonitorCardHeader
import com.cpumonitor.core.designsystem.model.toPercentDisplay
import com.cpumonitor.core.designsystem.theme.MonitorColors
import com.cpumonitor.core.designsystem.theme.MonitorDimens
import com.cpumonitor.domain.model.analytics.AdvancedAnalyticsSnapshot
import com.cpumonitor.domain.model.analytics.AnalyticsDashboard
import com.cpumonitor.domain.model.analytics.AnalyticsInsight
import com.cpumonitor.domain.model.analytics.ComponentHealthScore
import com.cpumonitor.domain.model.analytics.DeviceHealthReport
import com.cpumonitor.domain.model.analytics.HealthStatus
import com.cpumonitor.domain.model.analytics.PerformanceScore
import com.cpumonitor.domain.model.history.HistoryTimeRange
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when {
            uiState.isLoading && uiState.dashboard == null -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }

            uiState.dashboard != null -> {
                AnalyticsContent(
                    dashboard = uiState.dashboard!!,
                    selectedWindow = uiState.selectedWindow,
                    onWindowSelected = viewModel::selectWindow,
                    contentPadding = padding,
                )
            }

            else -> {
                Text(
                    text = uiState.errorMessage ?: stringResource(R.string.analytics_unavailable),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnalyticsContent(
    dashboard: AnalyticsDashboard,
    selectedWindow: HistoryTimeRange,
    onWindowSelected: (HistoryTimeRange) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(MonitorDimens.spacingLg),
        verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
    ) {
        item {
            Text(
                text = stringResource(R.string.analytics_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        }

        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm)) {
                HistoryTimeRange.entries.forEach { window ->
                    FilterChip(
                        selected = selectedWindow == window,
                        onClick = { onWindowSelected(window) },
                        label = { Text(window.localizedName()) },
                    )
                }
            }
        }

        item {
            DeviceHealthCard(health = dashboard.health)
        }

        item {
            PerformanceScoreCard(performance = dashboard.performance)
        }

        item {
            AdvancedAnalyticsCard(analytics = dashboard.analytics)
        }

        items(dashboard.analytics.insights, key = { it.title }) { insight ->
            InsightCard(insight = insight)
        }
    }
}

@Composable
private fun DeviceHealthCard(health: DeviceHealthReport) {
    MonitorCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
        ) {
            MonitorCardHeader(
                title = stringResource(R.string.analytics_health_engine),
                subtitle = health.status.localizedName(),
            )
            Text(
                text = health.overallScore.toString(),
                style = MaterialTheme.typography.displaySmall,
                color = MonitorColors.HealthScore,
            )
            health.components.forEach { component ->
                ComponentHealthLine(component = component)
            }
        }
    }
}

@Composable
private fun ComponentHealthLine(component: ComponentHealthScore) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = component.localizedDisplayLine(),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = component.summary.localizedHealthComponentSummary(),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun PerformanceScoreCard(performance: PerformanceScore) {
    MonitorCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingXs),
        ) {
            MonitorCardHeader(
                title = stringResource(R.string.analytics_performance_scoring),
                subtitle = stringResource(R.string.analytics_overall_score, performance.overallScore),
            )
            ScoreLine(stringResource(R.string.analytics_cpu_stability), performance.cpuStabilityScore)
            ScoreLine(stringResource(R.string.analytics_thermal_headroom), performance.thermalHeadroomScore)
            ScoreLine(stringResource(R.string.analytics_memory_efficiency), performance.memoryEfficiencyScore)
            ScoreLine(stringResource(R.string.analytics_battery_endurance), performance.batteryEnduranceScore)
            Text(
                text = performance.summary.localizedPerformanceSummary(),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ScoreLine(label: String, score: Int) {
    Text(
        text = "$label: $score",
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun AdvancedAnalyticsCard(analytics: AdvancedAnalyticsSnapshot) {
    MonitorCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
        ) {
            MonitorCardHeader(
                title = stringResource(R.string.analytics_trends_peaks),
                subtitle = stringResource(R.string.analytics_samples, analytics.sampleCount),
            )
            PeakLine(stringResource(R.string.analytics_peak_cpu), analytics.peakCpuPercent.toPercentDisplay())
            PeakLine(stringResource(R.string.analytics_peak_memory), analytics.peakMemoryPercent.toPercentDisplay())
            PeakLine(
                stringResource(R.string.analytics_peak_temperature),
                "${analytics.peakTemperatureCelsius.roundToInt()}°C",
            )
            analytics.trends.forEach { trend ->
                Text(
                    text = trend.localizedDisplayLine(),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun PeakLine(label: String, value: String) {
    Text(text = "$label: $value", style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun InsightCard(insight: AnalyticsInsight) {
    MonitorCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingXs),
        ) {
            MonitorCardHeader(
                title = insight.localizedTitle(),
                subtitle = insight.severity.localizedName(),
            )
            Text(text = insight.localizedDescription(), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
