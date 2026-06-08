package com.cpumonitor.feature.benchmark.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cpumonitor.core.ui.R
import com.cpumonitor.core.ui.localized.localizedName
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cpumonitor.core.designsystem.component.MonitorCard
import com.cpumonitor.core.designsystem.component.MonitorCardHeader
import com.cpumonitor.core.designsystem.model.toPercentDisplay
import com.cpumonitor.core.designsystem.theme.MonitorDimens
import com.cpumonitor.domain.model.benchmark.BenchmarkMode
import com.cpumonitor.domain.model.benchmark.BenchmarkResult
import com.cpumonitor.domain.model.benchmark.StressTestDuration
import com.cpumonitor.domain.model.benchmark.StressTestResult
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BenchmarkScreen(
    viewModel: BenchmarkViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(MonitorDimens.spacingLg),
            verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
        ) {
            item {
                Text(
                    text = stringResource(R.string.benchmark_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }

            if (uiState.isRunning) {
                item {
                    ProgressCard(
                        progressPercent = uiState.progress.progressPercent,
                        elapsedMillis = uiState.progress.elapsedMillis,
                        totalMillis = uiState.progress.totalMillis,
                        temperature = uiState.progress.currentTemperatureCelsius,
                        frequencyMhz = uiState.progress.currentFrequencyMhz,
                        onCancel = viewModel::cancelSession,
                    )
                }
            }

            item {
                BenchmarkModeCard(
                    selectedMode = uiState.selectedMode,
                    enabled = !uiState.isRunning,
                    onModeSelected = viewModel::selectMode,
                    onRun = viewModel::runBenchmark,
                )
            }

            uiState.lastBenchmarkResult?.let { result ->
                item {
                    BenchmarkResultCard(result = result)
                }
            }

            item {
                StressTestCard(
                    selectedDuration = uiState.selectedStressDuration,
                    enabled = !uiState.isRunning,
                    onDurationSelected = viewModel::selectStressDuration,
                    onRun = viewModel::runStressTest,
                )
            }

            uiState.lastStressResult?.let { result ->
                item {
                    StressTestResultCard(result = result)
                }
            }

            uiState.errorMessage?.let { message ->
                item {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressCard(
    progressPercent: Float,
    elapsedMillis: Long,
    totalMillis: Long,
    temperature: Float?,
    frequencyMhz: Float?,
    onCancel: () -> Unit,
) {
    MonitorCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
        ) {
            MonitorCardHeader(
                title = stringResource(R.string.benchmark_running),
                subtitle = "${formatDuration(elapsedMillis)} / ${formatDuration(totalMillis)}",
            )
            LinearProgressIndicator(
                progress = { progressPercent / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "${progressPercent.roundToInt()}%",
                style = MaterialTheme.typography.bodyMedium,
            )
            temperature?.let {
                Text(
                    text = stringResource(R.string.benchmark_temperature_fmt, it.roundToInt()),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            frequencyMhz?.let {
                Text(
                    text = stringResource(R.string.benchmark_frequency_fmt, it.roundToInt()),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.benchmark_cancel))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BenchmarkModeCard(
    selectedMode: BenchmarkMode,
    enabled: Boolean,
    onModeSelected: (BenchmarkMode) -> Unit,
    onRun: () -> Unit,
) {
    MonitorCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
        ) {
            MonitorCardHeader(
                title = stringResource(R.string.benchmark_cpu_title),
                subtitle = stringResource(R.string.benchmark_cpu_subtitle),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm)) {
                BenchmarkMode.entries.forEach { mode ->
                    FilterChip(
                        selected = selectedMode == mode,
                        onClick = { onModeSelected(mode) },
                        enabled = enabled,
                        label = { Text(mode.localizedName()) },
                    )
                }
            }
            Button(
                onClick = onRun,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.benchmark_run_cpu))
            }
        }
    }
}

@Composable
private fun BenchmarkResultCard(result: BenchmarkResult) {
    MonitorCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingXs),
        ) {
            MonitorCardHeader(
                title = stringResource(R.string.benchmark_result),
                subtitle = result.mode.localizedName(),
            )
            ResultLine(stringResource(R.string.benchmark_score), result.score.toString())
            ResultLine(
                stringResource(R.string.benchmark_peak_temperature),
                "${result.peakTemperatureCelsius.roundToInt()}°C",
            )
            ResultLine(
                stringResource(R.string.benchmark_avg_temperature),
                "${result.averageTemperatureCelsius.roundToInt()}°C",
            )
            ResultLine(
                stringResource(R.string.benchmark_frequency_stability),
                result.frequencyStabilityPercent.toPercentDisplay(),
            )
            ResultLine(stringResource(R.string.benchmark_duration), formatDuration(result.durationMillis))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StressTestCard(
    selectedDuration: StressTestDuration,
    enabled: Boolean,
    onDurationSelected: (StressTestDuration) -> Unit,
    onRun: () -> Unit,
) {
    MonitorCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
        ) {
            MonitorCardHeader(
                title = stringResource(R.string.benchmark_stress_title),
                subtitle = stringResource(R.string.benchmark_stress_subtitle),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm)) {
                StressTestDuration.entries.forEach { duration ->
                    FilterChip(
                        selected = selectedDuration == duration,
                        onClick = { onDurationSelected(duration) },
                        enabled = enabled,
                        label = { Text(duration.localizedName()) },
                    )
                }
            }
            Button(
                onClick = onRun,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.benchmark_run_stress))
            }
        }
    }
}

@Composable
private fun StressTestResultCard(result: StressTestResult) {
    MonitorCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingXs),
        ) {
            MonitorCardHeader(
                title = stringResource(R.string.benchmark_stress_result),
                subtitle = result.duration.localizedName(),
            )
            ResultLine(
                stringResource(R.string.benchmark_peak_temperature),
                "${result.peakTemperatureCelsius.roundToInt()}°C",
            )
            ResultLine(
                stringResource(R.string.benchmark_avg_temperature),
                "${result.averageTemperatureCelsius.roundToInt()}°C",
            )
            ResultLine(
                stringResource(R.string.benchmark_frequency_stability),
                result.frequencyStabilityPercent.toPercentDisplay(),
            )
            ResultLine(
                stringResource(R.string.benchmark_avg_cpu),
                result.averageCpuUsagePercent.toPercentDisplay(),
            )
            ResultLine(stringResource(R.string.benchmark_duration), formatDuration(result.durationMillis))
        }
    }
}

@Composable
private fun ResultLine(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1_000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return if (minutes > 0L) {
        "${minutes}m ${seconds}s"
    } else {
        "${seconds}s"
    }
}
