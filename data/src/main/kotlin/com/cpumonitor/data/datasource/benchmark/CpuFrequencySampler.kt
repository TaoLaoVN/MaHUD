package com.cpumonitor.data.datasource.benchmark

import java.io.File

internal object CpuFrequencySampler {

    fun readAverageFrequencyMhz(): Float? {
        val coreCount = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        val frequenciesKhz = (0 until coreCount).mapNotNull(::readCoreFrequencyKhz)
        if (frequenciesKhz.isEmpty()) return null
        return frequenciesKhz.average().toFloat() / 1_000f
    }

    private fun readCoreFrequencyKhz(coreIndex: Int): Int? {
        val file = File("/sys/devices/system/cpu/cpu$coreIndex/cpufreq/scaling_cur_freq")
        if (!file.canRead()) return null
        return file.readText().trim().toIntOrNull()
    }
}
