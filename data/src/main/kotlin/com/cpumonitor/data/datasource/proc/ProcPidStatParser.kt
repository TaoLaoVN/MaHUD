package com.cpumonitor.data.datasource.proc

/**
 * Parses `/proc/[pid]/stat` utime/stime counters for per-process CPU sampling.
 */
internal object ProcPidStatParser {

    data class PidCpuSnapshot(
        val pid: Int,
        val timestampMillis: Long,
        val totalJiffies: Long,
    )

    fun parse(pid: Int, content: String, timestampMillis: Long = System.currentTimeMillis()): PidCpuSnapshot? {
        val closeIndex = content.indexOf(')')
        if (closeIndex < 0 || closeIndex + 2 >= content.length) return null

        val fields = content.substring(closeIndex + 2).trim().split(Regex("\\s+"))
        if (fields.size < 13) return null

        val utime = fields[11].toLongOrNull() ?: return null
        val stime = fields[12].toLongOrNull() ?: return null

        return PidCpuSnapshot(
            pid = pid,
            timestampMillis = timestampMillis,
            totalJiffies = utime + stime,
        )
    }

    fun calculateUsagePercent(previous: PidCpuSnapshot, current: PidCpuSnapshot): Float {
        val timeDelta = current.timestampMillis - previous.timestampMillis
        if (timeDelta <= 0L) return 0f

        val jiffiesDelta = current.totalJiffies - previous.totalJiffies
        if (jiffiesDelta <= 0L) return 0f

        val usage = (jiffiesDelta.toDouble() / timeDelta.toDouble()) * 100.0
        return usage.toFloat().coerceIn(0f, 100f)
    }
}
