package com.cpumonitor.domain.analytics

import com.cpumonitor.domain.model.analytics.MetricSnapshotBundle
import com.cpumonitor.domain.model.metric.CpuMetric
import org.junit.Assert.assertTrue
import org.junit.Test

class PerformanceScoreEngineTest {

    @Test
    fun evaluate_returnsHighScoreForStableCpuSamples() {
        val metrics = (0 until 10).map { index ->
            CpuMetric(
                timestampMillis = index * 1_000L,
                totalUsagePercent = 20f + (index % 2),
                perCoreUsagePercent = listOf(20f),
                bigCoreUsagePercent = 20f,
                littleCoreUsagePercent = 20f,
                currentFrequencyMhz = 1_800f,
                minFrequencyMhz = 500f,
                maxFrequencyMhz = 2_000f,
            )
        }

        val score = PerformanceScoreEngine.evaluate(
            MetricSnapshotBundle(
                cpuMetrics = metrics,
                memoryMetrics = emptyList(),
                thermalMetrics = emptyList(),
                batteryMetrics = emptyList(),
            ),
        )

        assertTrue(score.cpuStabilityScore >= 90)
        assertTrue(score.overallScore >= 50)
    }
}
