package com.cpumonitor.core.monitoring

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MonitoringOverheadTrackerTest {

    @Before
    fun setUp() {
        MonitoringOverheadTracker.reset()
    }

    @Test
    fun snapshot_calculatesAverageOverheadAcrossSamples() {
        MonitoringOverheadTracker.recordPoll(durationMs = 10, intervalMs = 1_000)
        MonitoringOverheadTracker.recordPoll(durationMs = 20, intervalMs = 1_000)

        val snapshot = MonitoringOverheadTracker.snapshot()

        assertEquals(1.5f, snapshot.overheadPercent, 0.01f)
        assertTrue(snapshot.withinBudget)
        assertEquals(2L, snapshot.pollCount)
    }

    @Test
    fun snapshot_flagsBudgetViolation() {
        MonitoringOverheadTracker.recordPoll(durationMs = 30, intervalMs = 1_000)

        val snapshot = MonitoringOverheadTracker.snapshot()

        assertEquals(3f, snapshot.overheadPercent, 0.01f)
        assertTrue(!snapshot.withinBudget)
    }
}
