package com.cpumonitor.core.designsystem.theme

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cpumonitor.core.designsystem.component.MonitorCard
import com.cpumonitor.core.designsystem.component.MonitorMetricValue

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "AMOLED", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun MonitorThemePreview(
    themeMode: AppThemeMode = AppThemeMode.Light,
) {
    CPUMonitorTheme(themeMode = themeMode) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(MonitorDimens.spacingLg),
                verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingMd),
            ) {
                Text(
                    text = "Theme: ${themeMode.name}",
                    style = MaterialTheme.typography.titleLarge,
                )
                MonitorCard(modifier = Modifier.fillMaxWidth()) {
                    MonitorMetricValue(
                        value = "67.4",
                        unit = "%",
                        label = "CPU Usage",
                        valueColor = MonitorColors.CpuUsage,
                    )
                }
            }
        }
    }
}

@Preview(name = "Light Theme", showBackground = true)
@Composable
private fun LightThemePreview() {
    MonitorThemePreview(themeMode = AppThemeMode.Light)
}

@Preview(name = "Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DarkThemePreview() {
    MonitorThemePreview(themeMode = AppThemeMode.Dark)
}

@Preview(name = "AMOLED Theme", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AmoledThemePreview() {
    MonitorThemePreview(themeMode = AppThemeMode.Amoled)
}
