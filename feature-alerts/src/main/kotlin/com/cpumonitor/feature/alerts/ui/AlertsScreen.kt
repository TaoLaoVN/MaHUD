package com.cpumonitor.feature.alerts.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.cpumonitor.core.ui.R
import com.cpumonitor.core.ui.localized.localizedDefaultAlertRuleLabel
import com.cpumonitor.core.ui.localized.localizedMessage
import com.cpumonitor.core.ui.localized.localizedName
import com.cpumonitor.core.ui.localized.localizedSymbol
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cpumonitor.core.designsystem.component.MonitorCard
import com.cpumonitor.core.designsystem.component.MonitorCardHeader
import com.cpumonitor.core.designsystem.theme.MonitorDimens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when {
            uiState.isLoading -> CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )

            else -> AlertsContent(
                uiState = uiState,
                onToggleRule = viewModel::toggleRule,
                contentPadding = padding,
            )
        }
    }
}

@Composable
private fun AlertsContent(
    uiState: AlertsUiState,
    onToggleRule: (com.cpumonitor.domain.model.alert.AlertRule) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = MonitorDimens.spacingLg + contentPadding.calculateLeftPadding(
                androidx.compose.ui.unit.LayoutDirection.Ltr,
            ),
            end = MonitorDimens.spacingLg + contentPadding.calculateRightPadding(
                androidx.compose.ui.unit.LayoutDirection.Ltr,
            ),
            top = MonitorDimens.spacingLg + contentPadding.calculateTopPadding(),
            bottom = MonitorDimens.spacingLg + contentPadding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingMd),
    ) {
        item {
            MonitorCard {
                MonitorCardHeader(
                    title = stringResource(R.string.alerts_rules),
                    subtitle = stringResource(R.string.alerts_rules_subtitle),
                )
                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        modifier = Modifier.padding(top = MonitorDimens.spacingSm),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        items(uiState.rules, key = { it.id }) { rule ->
            MonitorCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = localizedDefaultAlertRuleLabel(rule.id, rule.label),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = rule.enabled,
                        onCheckedChange = { onToggleRule(rule) },
                    )
                }
                Text(
                    text = "${rule.metricType.localizedName()} ${rule.comparator.localizedSymbol()} ${rule.threshold}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            MonitorCard {
                MonitorCardHeader(
                    title = stringResource(R.string.alerts_history),
                    subtitle = stringResource(R.string.alerts_events, uiState.history.size),
                )
            }
        }

        if (uiState.history.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.alerts_none),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(uiState.history, key = { it.id }) { entry ->
                MonitorCard {
                    MonitorCardHeader(
                        title = entry.localizedMessage(),
                        subtitle = formatTimestamp(entry.timestampMillis),
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(timestampMillis: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestampMillis))
