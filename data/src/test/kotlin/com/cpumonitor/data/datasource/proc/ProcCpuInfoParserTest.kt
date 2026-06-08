package com.cpumonitor.data.datasource.proc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ProcCpuInfoParserTest {

    @Test
    fun `parse extracts processor blocks and hardware`() {
        val content = """
            processor       : 0
            model name      : ARM Cortex-A78
            cpu MHz         : 1800.000
            CPU implementer : 0x41
            CPU architecture: 8
            CPU variant     : 0x1
            CPU part        : 0xd41

            processor       : 1
            model name      : ARM Cortex-A55
            cpu MHz         : 1200.000

            Hardware        : Qualcomm Technologies, Inc SM8550
        """.trimIndent()

        val info = ProcCpuInfoParser.parse(content, abi = "arm64-v8a")

        assertEquals(2, info.coreCount)
        assertEquals("arm64-v8a", info.abi)
        assertEquals("Qualcomm Technologies, Inc SM8550", info.hardware)

        val core0 = info.processors[0]
        assertEquals(0, core0.index)
        assertEquals("ARM Cortex-A78", core0.modelName)
        assertEquals(1800f, core0.currentFrequencyMhz!!, 0.01f)
        assertEquals("0x41", core0.implementer)

        val core1 = info.processors[1]
        assertEquals(1, core1.index)
        assertNotNull(core1.modelName)
    }
}
