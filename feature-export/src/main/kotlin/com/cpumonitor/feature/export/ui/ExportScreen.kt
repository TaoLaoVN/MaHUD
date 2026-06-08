package com.cpumonitor.feature.export.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.cpumonitor.core.ui.R
import com.cpumonitor.core.ui.localized.localizedName
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cpumonitor.core.designsystem.component.MonitorCard
import com.cpumonitor.core.designsystem.component.MonitorCardHeader
import com.cpumonitor.core.designsystem.theme.MonitorDimens
import com.cpumonitor.domain.model.export.ExportFormat
import com.cpumonitor.domain.model.export.ExportedReport
import com.cpumonitor.domain.model.history.HistoryTimeRange
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExportScreen(
    viewModel: ExportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = MonitorDimens.spacingLg + padding.calculateLeftPadding(
                    androidx.compose.ui.unit.LayoutDirection.Ltr,
                ),
                end = MonitorDimens.spacingLg + padding.calculateRightPadding(
                    androidx.compose.ui.unit.LayoutDirection.Ltr,
                ),
                top = MonitorDimens.spacingLg + padding.calculateTopPadding(),
                bottom = MonitorDimens.spacingLg + padding.calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
        ) {
            item {
                MonitorCard {
                    MonitorCardHeader(
                        title = stringResource(R.string.export_report),
                        subtitle = stringResource(R.string.export_subtitle),
                    )
                    Text(
                        text = stringResource(R.string.export_local_hint),
                        modifier = Modifier.padding(top = MonitorDimens.spacingSm),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                MonitorCard {
                    MonitorCardHeader(title = stringResource(R.string.export_format))
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = MonitorDimens.spacingMd),
                        horizontalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
                    ) {
                        ExportFormat.entries.forEach { format ->
                            FilterChip(
                                selected = uiState.selectedFormat == format,
                                onClick = { viewModel.selectFormat(format) },
                                label = { Text(format.localizedName()) },
                            )
                        }
                    }
                }
            }

            item {
                MonitorCard {
                    MonitorCardHeader(title = stringResource(R.string.export_time_range))
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = MonitorDimens.spacingMd),
                        horizontalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
                    ) {
                        HistoryTimeRange.entries.forEach { range ->
                            FilterChip(
                                selected = uiState.selectedTimeRange == range,
                                onClick = { viewModel.selectTimeRange(range) },
                                label = { Text(range.localizedName()) },
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = viewModel::exportReport,
                    enabled = !uiState.isExporting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (uiState.isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = MonitorDimens.spacingSm),
                            strokeWidth = 2.dp,
                        )
                    }
                    Text(
                        text = if (uiState.isExporting) {
                            stringResource(R.string.exporting)
                        } else {
                            stringResource(R.string.export_report)
                        },
                    )
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

            uiState.lastExport?.let { export ->
                item {
                    MonitorCard {
                        MonitorCardHeader(
                            title = stringResource(R.string.export_last),
                            subtitle = export.fileName,
                        )
                        Text(
                            text = stringResource(R.string.export_size_fmt, export.bytesWritten),
                            modifier = Modifier.padding(top = MonitorDimens.spacingSm),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = export.absolutePath,
                            modifier = Modifier.padding(top = MonitorDimens.spacingXs),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Button(
                            onClick = {
                                shareExport(
                                    packageName = context.packageName,
                                    context = context,
                                    export = export,
                                    chooserTitle = context.getString(R.string.export_share_chooser),
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = MonitorDimens.spacingMd),
                        ) {
                            Text(stringResource(R.string.export_share))
                        }
                    }
                }
            }
        }
    }
}

private fun shareExport(
    packageName: String,
    context: android.content.Context,
    export: ExportedReport,
    chooserTitle: String,
) {
    val file = File(export.absolutePath)
    val uri = FileProvider.getUriForFile(
        context,
        "$packageName.fileprovider",
        file,
    )
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = export.mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
}
