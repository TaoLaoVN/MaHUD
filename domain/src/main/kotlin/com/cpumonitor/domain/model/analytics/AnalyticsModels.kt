package com.cpumonitor.domain.model.analytics

enum class TrendDirection {
    UP,
    DOWN,
    STABLE,
}

enum class HealthStatus(val displayName: String) {
    EXCELLENT("Excellent"),
    GOOD("Good"),
    FAIR("Fair"),
    POOR("Poor"),
    CRITICAL("Critical"),
}

data class ComponentHealthScore(
    val component: String,
    val score: Int,
    val status: HealthStatus,
    val summary: String,
)

data class DeviceHealthReport(
    val overallScore: Int,
    val status: HealthStatus,
    val components: List<ComponentHealthScore>,
    val timestampMillis: Long,
)

data class PerformanceScore(
    val overallScore: Int,
    val cpuStabilityScore: Int,
    val thermalHeadroomScore: Int,
    val memoryEfficiencyScore: Int,
    val batteryEnduranceScore: Int,
    val summary: String,
)

data class MetricTrend(
    val metricName: String,
    val direction: TrendDirection,
    val changePercent: Float,
    val currentAverage: Float,
    val previousAverage: Float,
)

data class AnalyticsInsight(
    val title: String,
    val description: String,
    val severity: HealthStatus,
)

data class AdvancedAnalyticsSnapshot(
    val trends: List<MetricTrend>,
    val insights: List<AnalyticsInsight>,
    val peakCpuPercent: Float,
    val peakMemoryPercent: Float,
    val peakTemperatureCelsius: Float,
    val sampleCount: Int,
    val windowMillis: Long,
)

data class AnalyticsDashboard(
    val health: DeviceHealthReport,
    val performance: PerformanceScore,
    val analytics: AdvancedAnalyticsSnapshot,
)

data class MetricSnapshotBundle(
    val cpuMetrics: List<com.cpumonitor.domain.model.metric.CpuMetric>,
    val memoryMetrics: List<com.cpumonitor.domain.model.metric.MemoryMetric>,
    val thermalMetrics: List<com.cpumonitor.domain.model.metric.ThermalMetric>,
    val batteryMetrics: List<com.cpumonitor.domain.model.metric.BatteryMetric>,
)
