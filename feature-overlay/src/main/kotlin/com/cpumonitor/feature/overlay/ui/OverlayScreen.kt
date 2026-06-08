package com.cpumonitor.feature.overlay.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cpumonitor.core.ui.R
import com.cpumonitor.domain.model.OverlayMetrics
import com.cpumonitor.feature.overlay.permission.OverlayPermissionEffect
import com.cpumonitor.feature.overlay.permission.openOverlayPermissionSettings
import kotlin.math.roundToInt

@Composable
fun OverlayScreen(
    viewModel: OverlayViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    OverlayPermissionEffect(onPermissionChanged = viewModel::onPermissionResult)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.overlay_title),
            style = MaterialTheme.typography.headlineSmall,
        )

        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            else -> {
                OverlayHeroCard(
                    uiState = uiState,
                    onStartOverlay = viewModel::startOverlay,
                    onStopOverlay = viewModel::stopOverlay,
                    onRequestPermission = { openOverlayPermissionSettings(context) },
                )

                PermissionCard(
                    isGranted = uiState.isPermissionGranted,
                    onRequestPermission = { openOverlayPermissionSettings(context) },
                )

                GamingModeCard(
                    enabled = uiState.gamingModeEnabled,
                    onToggle = viewModel::setGamingModeEnabled,
                )

                uiState.previewMetrics?.let { metrics ->
                    MetricsPreviewCard(metrics = metrics)
                }

                uiState.errorMessage?.let { message ->
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
private fun OverlayHeroCard(
    uiState: OverlayUiState,
    onStartOverlay: () -> Unit,
    onStopOverlay: () -> Unit,
    onRequestPermission: () -> Unit,
) {
    val status = when {
        uiState.isOverlayRunning -> stringResource(R.string.overlay_status_running)
        uiState.isPermissionGranted -> stringResource(R.string.overlay_status_ready)
        else -> stringResource(R.string.overlay_status_blocked)
    }
    val summary = when {
        uiState.isOverlayRunning -> stringResource(R.string.overlay_primary_running)
        uiState.isPermissionGranted -> stringResource(R.string.overlay_primary_ready)
        else -> stringResource(R.string.overlay_primary_blocked)
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.overlay_mvp_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (uiState.isOverlayRunning) {
                    OutlinedButton(
                        onClick = onStopOverlay,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.overlay_stop))
                    }
                } else {
                    Button(
                        onClick = if (uiState.isPermissionGranted) onStartOverlay else onRequestPermission,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            stringResource(
                                if (uiState.isPermissionGranted) {
                                    R.string.overlay_start
                                } else {
                                    R.string.overlay_grant
                                },
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(
    isGranted: Boolean,
    onRequestPermission: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.overlay_permission),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isGranted) {
                    stringResource(R.string.overlay_permission_granted)
                } else {
                    stringResource(R.string.overlay_permission_required)
                },
                style = MaterialTheme.typography.bodyMedium,
            )
            if (!isGranted) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onRequestPermission) {
                    Text(stringResource(R.string.overlay_grant))
                }
            }
        }
    }
}

@Composable
private fun GamingModeCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.overlay_gaming),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(R.string.overlay_gaming_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
            )
        }
    }
}

@Composable
private fun MetricsPreviewCard(metrics: OverlayMetrics) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.overlay_preview),
                style = MaterialTheme.typography.titleMedium,
            )
            MetricPreviewLine(stringResource(R.string.metric_cpu), "${metrics.cpuUsagePercent.roundToInt()}%")
            MetricPreviewLine(stringResource(R.string.metric_memory), "${metrics.ramUsagePercent.roundToInt()}%")
            MetricPreviewLine(stringResource(R.string.label_temperature), "${metrics.temperatureCelsius.roundToInt()}°C")
            MetricPreviewLine(stringResource(R.string.overlay_metric_fps), metrics.fps.toString())
        }
    }
}

@Composable
private fun MetricPreviewLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
