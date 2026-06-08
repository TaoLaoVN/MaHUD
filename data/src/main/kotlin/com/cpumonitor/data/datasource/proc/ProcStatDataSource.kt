package com.cpumonitor.data.datasource.proc

import com.cpumonitor.data.datasource.SystemDataSource
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads and parses `/proc/stat` for CPU utilization sampling.
 */
@Singleton
class ProcStatDataSource @Inject constructor() : SystemDataSource {

    private val statFile = File(PROC_STAT_PATH)

    internal fun readSnapshot(): ProcStatSnapshot {
        return try {
            val content = statFile.readText()
            ProcStatParser.parse(content)
        } catch (_: Exception) {
            // Some Android devices/ROMs restrict reading `/proc/stat` (e.g. SELinux => EACCES).
            // Returning a safe zero snapshot prevents app/service crashes and allows UI to degrade gracefully.
            val now = System.currentTimeMillis()
            val zeroCounters = CpuTimeCounters(
                user = 0L,
                nice = 0L,
                system = 0L,
                idle = 0L,
                iowait = 0L,
                irq = 0L,
                softirq = 0L,
                steal = 0L,
            )
            ProcStatSnapshot(
                timestampMillis = now,
                aggregate = zeroCounters,
                perCore = emptyList(),
            )
        }
    }

    companion object {
        const val PROC_STAT_PATH = "/proc/stat"
    }
}
