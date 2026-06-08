package com.cpumonitor.domain.performance

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PerformanceBudgetTest {

    @Test
    fun isCpuOverheadWithinBudget_acceptsValuesAtOrBelowTwoPercent() {
        assertTrue(PerformanceBudget.isCpuOverheadWithinBudget(0f))
        assertTrue(PerformanceBudget.isCpuOverheadWithinBudget(2f))
        assertFalse(PerformanceBudget.isCpuOverheadWithinBudget(2.1f))
    }

    @Test
    fun isMemoryWithinBudget_acceptsValuesAtOrBelow150Mb() {
        val withinBudget = 140L * 1024L * 1024L
        val overBudget = 160L * 1024L * 1024L

        assertTrue(PerformanceBudget.isMemoryWithinBudget(withinBudget))
        assertFalse(PerformanceBudget.isMemoryWithinBudget(overBudget))
    }
}
