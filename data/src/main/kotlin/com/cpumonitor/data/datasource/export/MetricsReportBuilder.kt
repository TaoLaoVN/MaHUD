package com.cpumonitor.data.datasource.export

import com.cpumonitor.domain.model.metric.BatteryMetric
import com.cpumonitor.domain.model.metric.CpuMetric
import com.cpumonitor.domain.model.metric.MemoryMetric
import com.cpumonitor.domain.model.metric.ThermalMetric

internal data class MetricsReportSnapshot(
    val cpuMetrics: List<CpuMetric>,
    val memoryMetrics: List<MemoryMetric>,
    val thermalMetrics: List<ThermalMetric>,
    val batteryMetrics: List<BatteryMetric>,
    val sinceMillis: Long,
    val generatedAtMillis: Long,
)

internal object MetricsReportBuilder {

    fun buildCsv(snapshot: MetricsReportSnapshot): String = buildString {
        appendLine("CPU Monitor Diagnostic Report")
        appendLine("generated_at,${snapshot.generatedAtMillis}")
        appendLine("since_millis,${snapshot.sinceMillis}")
        appendLine()
        appendLine("cpu_timestamp,total_usage_percent,current_frequency_mhz")
        snapshot.cpuMetrics.forEach { metric ->
            appendLine("${metric.timestampMillis},${metric.totalUsagePercent},${metric.currentFrequencyMhz}")
        }
        appendLine()
        appendLine("memory_timestamp,used_bytes,available_bytes,free_bytes,cached_bytes")
        snapshot.memoryMetrics.forEach { metric ->
            appendLine(
                "${metric.timestampMillis},${metric.usedBytes},${metric.availableBytes}," +
                    "${metric.freeBytes},${metric.cachedBytes}",
            )
        }
        appendLine()
        appendLine("thermal_timestamp,cpu_temperature_celsius,battery_temperature_celsius,is_overheating")
        snapshot.thermalMetrics.forEach { metric ->
            appendLine(
                "${metric.timestampMillis},${metric.cpuTemperatureCelsius}," +
                    "${metric.batteryTemperatureCelsius},${metric.isOverheating}",
            )
        }
        appendLine()
        appendLine("battery_timestamp,percentage,voltage_mv,current_ma,temperature_celsius,is_charging")
        snapshot.batteryMetrics.forEach { metric ->
            appendLine(
                "${metric.timestampMillis},${metric.percentage},${metric.voltageMv}," +
                    "${metric.currentMa},${metric.temperatureCelsius},${metric.isCharging}",
            )
        }
    }

    fun buildJson(snapshot: MetricsReportSnapshot): String = buildString {
        appendLine("{")
        appendLine("  \"generatedAtMillis\": ${snapshot.generatedAtMillis},")
        appendLine("  \"sinceMillis\": ${snapshot.sinceMillis},")
        appendLine("  \"cpu\": ${cpuArray(snapshot.cpuMetrics)},")
        appendLine("  \"memory\": ${memoryArray(snapshot.memoryMetrics)},")
        appendLine("  \"thermal\": ${thermalArray(snapshot.thermalMetrics)},")
        appendLine("  \"battery\": ${batteryArray(snapshot.batteryMetrics)}")
        appendLine("}")
    }

    fun buildPdfLines(snapshot: MetricsReportSnapshot): List<String> = buildList {
        add("CPU Monitor Diagnostic Report")
        add("Generated: ${snapshot.generatedAtMillis}")
        add("Window start: ${snapshot.sinceMillis}")
        add("")
        add("CPU samples: ${snapshot.cpuMetrics.size}")
        add("Memory samples: ${snapshot.memoryMetrics.size}")
        add("Thermal samples: ${snapshot.thermalMetrics.size}")
        add("Battery samples: ${snapshot.batteryMetrics.size}")
        add("")
        snapshot.cpuMetrics.takeLast(10).forEach { metric ->
            add("CPU ${metric.timestampMillis}: ${metric.totalUsagePercent}%")
        }
        snapshot.memoryMetrics.takeLast(5).forEach { metric ->
            add("RAM ${metric.timestampMillis}: used=${metric.usedBytes}")
        }
        snapshot.thermalMetrics.takeLast(5).forEach { metric ->
            add("Thermal ${metric.timestampMillis}: ${metric.cpuTemperatureCelsius}C")
        }
        snapshot.batteryMetrics.takeLast(5).forEach { metric ->
            add("Battery ${metric.timestampMillis}: ${metric.percentage}%")
        }
    }

    private fun cpuArray(metrics: List<CpuMetric>): String =
        metrics.joinToString(prefix = "[", postfix = "]") { metric ->
            """{"timestampMillis":${metric.timestampMillis},"totalUsagePercent":${metric.totalUsagePercent},"currentFrequencyMhz":${metric.currentFrequencyMhz}}"""
        }

    private fun memoryArray(metrics: List<MemoryMetric>): String =
        metrics.joinToString(prefix = "[", postfix = "]") { metric ->
            """{"timestampMillis":${metric.timestampMillis},"usedBytes":${metric.usedBytes},"availableBytes":${metric.availableBytes},"freeBytes":${metric.freeBytes},"cachedBytes":${metric.cachedBytes}}"""
        }

    private fun thermalArray(metrics: List<ThermalMetric>): String =
        metrics.joinToString(prefix = "[", postfix = "]") { metric ->
            """{"timestampMillis":${metric.timestampMillis},"cpuTemperatureCelsius":${metric.cpuTemperatureCelsius},"batteryTemperatureCelsius":${metric.batteryTemperatureCelsius},"isOverheating":${metric.isOverheating}}"""
        }

    private fun batteryArray(metrics: List<BatteryMetric>): String =
        metrics.joinToString(prefix = "[", postfix = "]") { metric ->
            """{"timestampMillis":${metric.timestampMillis},"percentage":${metric.percentage},"voltageMv":${metric.voltageMv},"currentMa":${metric.currentMa},"temperatureCelsius":${metric.temperatureCelsius},"isCharging":${metric.isCharging}}"""
        }
}
