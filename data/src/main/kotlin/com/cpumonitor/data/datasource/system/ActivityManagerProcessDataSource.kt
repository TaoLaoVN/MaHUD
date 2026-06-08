package com.cpumonitor.data.datasource.system

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import com.cpumonitor.data.datasource.SystemDataSource
import com.cpumonitor.data.datasource.proc.ProcPidStatParser
import com.cpumonitor.domain.model.process.ProcessSortOrder
import com.cpumonitor.domain.model.process.RunningProcess
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityManagerProcessDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : SystemDataSource {

    private val activityManager = context.getSystemService(ActivityManager::class.java)
    private val previousCpuSnapshots = mutableMapOf<Int, ProcPidStatParser.PidCpuSnapshot>()

    fun readRunningProcesses(sortOrder: ProcessSortOrder): List<RunningProcess> {
        val runningProcesses = activityManager.runningAppProcesses.orEmpty()
            .filter { process -> process.pid > 0 }
            .take(MAX_PROCESSES * 2)

        if (runningProcesses.isEmpty()) return emptyList()

        val pids = runningProcesses.map { it.pid }.toIntArray()
        val memoryInfos = activityManager.getProcessMemoryInfo(pids)
        val memoryByPid = runningProcesses.map { it.pid }
            .zip(memoryInfos.asIterable())
            .toMap()

        val processes = runningProcesses.mapNotNull { processInfo ->
            val memoryInfo = memoryByPid[processInfo.pid] ?: return@mapNotNull null
            val cpuUsage = readCpuUsagePercent(processInfo.pid)
            RunningProcess(
                pid = processInfo.pid,
                processName = processInfo.processName,
                cpuUsagePercent = cpuUsage,
                memoryPssKb = memoryInfo.totalPss,
                uid = processInfo.uid,
            )
        }

        return sortProcesses(processes, sortOrder).take(MAX_PROCESSES)
    }

    private fun readCpuUsagePercent(pid: Int): Float {
        val statFile = File("/proc/$pid/stat")
        if (!statFile.canRead()) return 0f

        val content = runCatching { statFile.readText() }.getOrNull() ?: return 0f
        val current = ProcPidStatParser.parse(pid, content) ?: return 0f
        val previous = previousCpuSnapshots.put(pid, current)
        return if (previous == null) {
            0f
        } else {
            ProcPidStatParser.calculateUsagePercent(previous, current)
        }
    }

    private fun sortProcesses(
        processes: List<RunningProcess>,
        sortOrder: ProcessSortOrder,
    ): List<RunningProcess> = when (sortOrder) {
        ProcessSortOrder.CPU -> processes.sortedByDescending { it.cpuUsagePercent }
        ProcessSortOrder.MEMORY -> processes.sortedByDescending { it.memoryPssKb }
        ProcessSortOrder.NAME -> processes.sortedBy { it.processName.lowercase() }
    }

    private companion object {
        const val MAX_PROCESSES = 50
    }
}
