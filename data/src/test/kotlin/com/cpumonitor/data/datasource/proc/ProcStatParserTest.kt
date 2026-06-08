package com.cpumonitor.data.datasource.proc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProcStatParserTest {

    @Test
    fun `parse extracts aggregate and per-core counters`() {
        val content = """
            cpu  100 200 300 400 10 20 30 40 0 0
            cpu0 10 20 30 40 1 2 3 4 0 0
            cpu1 20 30 40 50 2 3 4 5 0 0
            intr 12345
        """.trimIndent()

        val snapshot = ProcStatParser.parse(content, timestampMillis = 1_000L)

        assertEquals(1_000L, snapshot.timestampMillis)
        assertEquals(100L, snapshot.aggregate.user)
        assertEquals(2, snapshot.perCore.size)
        assertEquals(10L, snapshot.perCore[0].user)
        assertEquals(20L, snapshot.perCore[1].user)
    }

    @Test
    fun `calculateUsagePercent returns expected utilization`() {
        val previous = CpuTimeCounters(
            user = 100,
            nice = 0,
            system = 0,
            idle = 800,
            iowait = 0,
            irq = 0,
            softirq = 0,
            steal = 0,
        )
        val current = CpuTimeCounters(
            user = 150,
            nice = 0,
            system = 0,
            idle = 850,
            iowait = 0,
            irq = 0,
            softirq = 0,
            steal = 0,
        )

        val usage = ProcStatParser.calculateUsagePercent(previous, current)

        assertEquals(50f, usage, 0.01f)
    }

    @Test
    fun `calculateUsagePercent returns zero when total delta is non-positive`() {
        val counters = CpuTimeCounters(1, 1, 1, 1, 0, 0, 0, 0)
        assertEquals(0f, ProcStatParser.calculateUsagePercent(counters, counters))
    }
}
