package com.cpumonitor.core.ui.localized

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.cpumonitor.core.ui.R
import com.cpumonitor.domain.model.alert.AlertHistoryEntry
import com.cpumonitor.domain.model.analytics.AnalyticsInsight
import com.cpumonitor.domain.model.analytics.ComponentHealthScore
import com.cpumonitor.domain.model.analytics.MetricTrend
import com.cpumonitor.domain.model.settings.RetentionPeriod

private val AVG_USAGE_REGEX = Regex("""Average usage (\d+)%""")
private val PEAK_TEMP_REGEX = Regex("""Peak temperature (\d+)°C""")
private val BATTERY_SUMMARY_REGEX = Regex("""(\d+)% • (.+)""")
private val RISING_TITLE_REGEX = Regex("""^(.+) rising$""")
private val RISING_DESC_REGEX = Regex("""^(.+) increased (\d+)% in the last hour\.$""")
private val PEAK_CPU_DESC_REGEX = Regex("""Peak CPU usage reached (\d+)% in the selected window\.""")
private val PEAK_MEMORY_DESC_REGEX = Regex("""Peak memory usage reached (\d+)%\.""")
private val PEAK_TEMP_DESC_REGEX = Regex("""Peak temperature reached (\d+)°C\.""")
private val THROTTLE_DESC_REGEX = Regex("""(\d+) thermal throttling events recorded\.""")

@Composable
fun String.localizedBatteryHealth(): String = when (trim().lowercase()) {
    "good" -> stringResource(R.string.battery_health_good)
    "overheat" -> stringResource(R.string.battery_health_overheat)
    "dead" -> stringResource(R.string.battery_health_dead)
    "over voltage" -> stringResource(R.string.battery_health_over_voltage)
    "failure", "unspecified failure" -> stringResource(R.string.battery_health_failure)
    "cold" -> stringResource(R.string.battery_health_cold)
    "unknown" -> stringResource(R.string.battery_health_unknown)
    else -> this
}

@Composable
fun String.localizedThermalZoneName(): String {
    val key = trim().lowercase()
    return when {
        key.contains("cpu") || key.contains("cluster") || key.contains("ap") || key.contains("little") || key.contains("big") ->
            stringResource(R.string.thermal_zone_cpu)
        key.contains("gpu") -> stringResource(R.string.thermal_zone_gpu)
        key.contains("battery") -> stringResource(R.string.thermal_zone_battery)
        key.contains("skin") || key.contains("shell") || key.contains("surface") ->
            stringResource(R.string.thermal_zone_skin)
        key.contains("charger") || key.contains("usb") -> stringResource(R.string.thermal_zone_charger)
        key.contains("modem") || key.contains("cell") -> stringResource(R.string.thermal_zone_modem)
        key.contains("wlan") || key.contains("wifi") -> stringResource(R.string.thermal_zone_wlan)
        key.contains("cam") || key.contains("camera") -> stringResource(R.string.thermal_zone_camera)
        key.contains("pa") || key.contains("power_amp") -> stringResource(R.string.thermal_zone_pa)
        key.contains("soc") || key.contains("tsens") || key.contains("pack") ->
            stringResource(R.string.thermal_zone_soc)
        key.contains("lcd") || key.contains("display") -> stringResource(R.string.thermal_zone_display)
        key.contains("ntc") -> stringResource(R.string.thermal_zone_ntc)
        else -> this
    }
}

@Composable
fun String.localizedHealthComponentName(): String = when (trim()) {
    "CPU" -> stringResource(R.string.metric_cpu)
    "Memory" -> stringResource(R.string.metric_memory)
    "Thermal" -> stringResource(R.string.metric_thermal)
    "Battery" -> stringResource(R.string.metric_battery)
    else -> this
}

@Composable
fun String.localizedHealthComponentSummary(): String {
    AVG_USAGE_REGEX.matchEntire(this)?.let { match ->
        return stringResource(R.string.health_summary_avg_usage, match.groupValues[1].toInt())
    }
    PEAK_TEMP_REGEX.matchEntire(this)?.let { match ->
        return stringResource(R.string.health_summary_peak_temp, match.groupValues[1].toInt())
    }
    BATTERY_SUMMARY_REGEX.matchEntire(this)?.let { match ->
        val percent = match.groupValues[1].toInt()
        val health = match.groupValues[2].localizedBatteryHealth()
        return stringResource(R.string.health_summary_battery, percent, health)
    }
    return when (this) {
        "No CPU samples yet" -> stringResource(R.string.health_summary_no_cpu)
        "No memory samples yet" -> stringResource(R.string.health_summary_no_memory)
        "No thermal samples yet" -> stringResource(R.string.health_summary_no_thermal)
        "No battery samples yet" -> stringResource(R.string.health_summary_no_battery)
        else -> this
    }
}

@Composable
fun ComponentHealthScore.localizedDisplayLine(): String =
    "${component.localizedHealthComponentName()}: $score"

@Composable
fun String.localizedPerformanceSummary(): String = when (this) {
    "System performance is stable with healthy thermal headroom." ->
        stringResource(R.string.perf_summary_excellent)
    "Performance is good with minor variability." ->
        stringResource(R.string.perf_summary_good)
    "Performance is acceptable but shows some stress." ->
        stringResource(R.string.perf_summary_fair)
    "Performance is constrained by sustained load or heat." ->
        stringResource(R.string.perf_summary_poor)
    else -> this
}

@Composable
fun String.localizedMetricTrendName(): String = when (trim()) {
    "CPU usage" -> stringResource(R.string.trend_metric_cpu_usage)
    "Memory usage" -> stringResource(R.string.trend_metric_memory_usage)
    "Temperature" -> stringResource(R.string.trend_metric_temperature)
    else -> this
}

@Composable
fun MetricTrend.localizedDisplayLine(): String {
    val directionLabel = when (direction) {
        com.cpumonitor.domain.model.analytics.TrendDirection.UP -> "↑"
        com.cpumonitor.domain.model.analytics.TrendDirection.DOWN -> "↓"
        com.cpumonitor.domain.model.analytics.TrendDirection.STABLE -> "→"
    }
    return "${metricName.localizedMetricTrendName()} $directionLabel ${changePercent.toInt()}%"
}

@Composable
fun AnalyticsInsight.localizedTitle(): String = when (title) {
    "High CPU load detected" -> stringResource(R.string.insight_high_cpu_load)
    "Memory pressure detected" -> stringResource(R.string.insight_memory_pressure)
    "Elevated temperature" -> stringResource(R.string.insight_elevated_temperature)
    "Thermal throttling observed" -> stringResource(R.string.insight_thermal_throttling)
    "System stable" -> stringResource(R.string.insight_system_stable)
    else -> RISING_TITLE_REGEX.matchEntire(title)?.let { match ->
        val metric = match.groupValues[1].localizedMetricTrendName()
        stringResource(R.string.insight_title_rising, metric)
    } ?: title
}

@Composable
fun AnalyticsInsight.localizedDescription(): String {
    RISING_DESC_REGEX.matchEntire(description)?.let { match ->
        val metric = match.groupValues[1].localizedMetricTrendName()
        return stringResource(R.string.insight_desc_rising, metric, match.groupValues[2].toInt())
    }
    PEAK_CPU_DESC_REGEX.matchEntire(description)?.let { match ->
        return stringResource(R.string.insight_desc_peak_cpu, match.groupValues[1].toInt())
    }
    PEAK_MEMORY_DESC_REGEX.matchEntire(description)?.let { match ->
        return stringResource(R.string.insight_desc_peak_memory, match.groupValues[1].toInt())
    }
    PEAK_TEMP_DESC_REGEX.matchEntire(description)?.let { match ->
        return stringResource(R.string.insight_desc_peak_temperature, match.groupValues[1].toInt())
    }
    THROTTLE_DESC_REGEX.matchEntire(description)?.let { match ->
        return stringResource(R.string.insight_desc_throttling, match.groupValues[1].toInt())
    }
    return when (description) {
        "No significant anomalies detected in the selected analytics window." ->
            stringResource(R.string.insight_desc_stable)
        else -> description
    }
}

@Composable
fun localizedDefaultAlertRuleLabel(ruleId: String, fallback: String): String = when (ruleId) {
    "cpu_high" -> stringResource(R.string.alert_rule_cpu_high)
    "thermal_high" -> stringResource(R.string.alert_rule_thermal_high)
    "memory_high" -> stringResource(R.string.alert_rule_memory_high)
    "battery_low" -> stringResource(R.string.alert_rule_battery_low)
    else -> fallback
}

@Composable
fun AlertHistoryEntry.localizedMessage(): String {
    val label = localizedDefaultAlertRuleLabel(ruleId, message.substringBefore(" (current="))
    val current = formatAlertMetricValue(ruleId, metricValue)
    return stringResource(R.string.alert_history_message, label, current)
}

private fun formatAlertMetricValue(ruleId: String, value: Float): String =
    if (ruleId == "thermal_high") {
        "${value.toInt()}°C"
    } else {
        "${value.toInt()}%"
    }

@Composable
fun RetentionPeriod.localizedName(): String = when (this) {
    RetentionPeriod.HOURS_24 -> stringResource(R.string.retention_24h)
    RetentionPeriod.DAYS_7 -> stringResource(R.string.retention_7d)
    RetentionPeriod.DAYS_30 -> stringResource(R.string.retention_30d)
    RetentionPeriod.UNLIMITED -> stringResource(R.string.retention_unlimited)
}
