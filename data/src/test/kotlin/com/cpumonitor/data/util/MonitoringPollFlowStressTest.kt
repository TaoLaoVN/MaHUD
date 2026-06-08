package com.cpumonitor.data.util

import com.cpumonitor.core.monitoring.MonitoringOverheadTracker
import com.cpumonitor.domain.performance.PerformanceBudget
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class MonitoringPollFlowStressTest {

    @Before
    fun setUp() {
        MonitoringOverheadTracker.reset()
    }

    @Test
    fun monitoringPollFlow_runsLongSessionWithinCpuBudget() = runTest {
        val pollCount = AtomicInteger(0)

        val values = monitoringPollFlow(intervalMs = 50L) {
            pollCount.incrementAndGet()
            pollCount.get()
        }.take(120).toList()

        assertEquals(120, values.size)
        assertEquals(120, pollCount.get())

        val snapshot = MonitoringOverheadTracker.snapshot()
        assertTrue(
            "Expected overhead <= ${PerformanceBudget.MAX_CPU_OVERHEAD_PERCENT}% but was ${snapshot.overheadPercent}%",
            snapshot.withinBudget,
        )
    }
}
