package com.cpumonitor.data.datasource.proc

/**
 * Raw CPU time counters from a single `/proc/stat` line.
 */
internal data class CpuTimeCounters(
    val user: Long,
    val nice: Long,
    val system: Long,
    val idle: Long,
    val iowait: Long,
    val irq: Long,
    val softirq: Long,
    val steal: Long,
) {
    val total: Long
        get() = user + nice + system + idle + iowait + irq + softirq + steal

    val active: Long
        get() = total - idle - iowait
}

/**
 * Parsed snapshot of `/proc/stat` at a point in time.
 */
internal data class ProcStatSnapshot(
    val timestampMillis: Long,
    val aggregate: CpuTimeCounters,
    val perCore: List<CpuTimeCounters>,
)

/**
 * Stateless parser for `/proc/stat` content.
 * Kept pure for unit testing and minimal allocation during hot-path reads.
 */
internal object ProcStatParser {

    private const val AGGREGATE_CPU_PREFIX = "cpu "
    private const val CORE_CPU_PREFIX = "cpu"

    fun parse(content: String, timestampMillis: Long = System.currentTimeMillis()): ProcStatSnapshot {
        var aggregate: CpuTimeCounters? = null
        val perCore = ArrayList<CpuTimeCounters>(8)

        for (line in content.lineSequence()) {
            when {
                line.startsWith(AGGREGATE_CPU_PREFIX) -> aggregate = parseCpuLine(line)
                line.startsWith(CORE_CPU_PREFIX) && line.length > 3 && line[3].isDigit() -> {
                    parseCpuLine(line)?.let(perCore::add)
                }
            }
        }

        requireNotNull(aggregate) { "Aggregate cpu line not found in /proc/stat" }

        return ProcStatSnapshot(
            timestampMillis = timestampMillis,
            aggregate = aggregate,
            perCore = perCore,
        )
    }

    fun calculateUsagePercent(previous: CpuTimeCounters, current: CpuTimeCounters): Float {
        val totalDelta = current.total - previous.total
        if (totalDelta <= 0L) return 0f
        val activeDelta = current.active - previous.active
        return ((activeDelta.toDouble() / totalDelta.toDouble()) * 100.0).toFloat()
    }

    private fun parseCpuLine(line: String): CpuTimeCounters? {
        val parts = line.trim().split(Regex("\\s+"))
        if (parts.size < 5) return null

        return CpuTimeCounters(
            user = parts[1].toLongOrNull() ?: return null,
            nice = parts[2].toLongOrNull() ?: return null,
            system = parts[3].toLongOrNull() ?: return null,
            idle = parts[4].toLongOrNull() ?: return null,
            iowait = parts.getOrNull(5)?.toLongOrNull() ?: 0L,
            irq = parts.getOrNull(6)?.toLongOrNull() ?: 0L,
            softirq = parts.getOrNull(7)?.toLongOrNull() ?: 0L,
            steal = parts.getOrNull(8)?.toLongOrNull() ?: 0L,
        )
    }
}
