package com.cpumonitor.data.datasource.proc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProcPidStatParserTest {

    @Test
    fun parse_extractsUtimeAndStimeFromProcStatLine() {
        val content = "1234 (com.example.app) S 1 1234 0 0 -1 1077952832 100 0 0 0 50 30 0 0 20 0 10 0 0 0 0 0 0 0 0 0 0 0 0"
        val snapshot = ProcPidStatParser.parse(pid = 1234, content = content, timestampMillis = 1_000L)

        assertEquals(1234, snapshot?.pid)
        assertEquals(1_000L, snapshot?.timestampMillis)
        assertEquals(80L, snapshot?.totalJiffies)
    }

    @Test
    fun parse_returnsNullForMalformedContent() {
        assertNull(ProcPidStatParser.parse(pid = 1, content = "invalid", timestampMillis = 0L))
    }

    @Test
    fun calculateUsagePercent_returnsExpectedUtilization() {
        val previous = ProcPidStatParser.PidCpuSnapshot(pid = 1, timestampMillis = 1_000L, totalJiffies = 100L)
        val current = ProcPidStatParser.PidCpuSnapshot(pid = 1, timestampMillis = 2_000L, totalJiffies = 150L)

        assertEquals(5f, ProcPidStatParser.calculateUsagePercent(previous, current), 0.01f)
    }

    @Test
    fun calculateUsagePercent_returnsZeroWhenDeltaNonPositive() {
        val snapshot = ProcPidStatParser.PidCpuSnapshot(pid = 1, timestampMillis = 1_000L, totalJiffies = 100L)
        assertEquals(0f, ProcPidStatParser.calculateUsagePercent(snapshot, snapshot))
    }
}
