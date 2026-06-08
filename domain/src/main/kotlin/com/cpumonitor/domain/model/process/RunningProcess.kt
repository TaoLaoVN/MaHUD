package com.cpumonitor.domain.model.process

/**
 * Snapshot of a running process for the process monitor module.
 */
data class RunningProcess(
    val pid: Int,
    val processName: String,
    val cpuUsagePercent: Float,
    val memoryPssKb: Int,
    val uid: Int,
)

enum class ProcessSortOrder {
    CPU,
    MEMORY,
    NAME,
}
