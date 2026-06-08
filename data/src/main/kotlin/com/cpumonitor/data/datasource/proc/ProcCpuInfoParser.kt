package com.cpumonitor.data.datasource.proc

import com.cpumonitor.domain.model.CpuArchitectureInfo
import com.cpumonitor.domain.model.CpuProcessorInfo

/**
 * Stateless parser for `/proc/cpuinfo` content.
 */
internal object ProcCpuInfoParser {

    fun parse(content: String, abi: String): CpuArchitectureInfo {
        val processors = mutableListOf<CpuProcessorInfo>()
        var currentIndex: Int? = null
        var modelName: String? = null
        var currentFrequencyMhz: Float? = null
        var maxFrequencyMhz: Float? = null
        var implementer: String? = null
        var architecture: String? = null
        var variant: String? = null
        var part: String? = null
        var hardware: String? = null

        fun flushProcessor() {
            val index = currentIndex ?: return
            processors += CpuProcessorInfo(
                index = index,
                modelName = modelName,
                currentFrequencyMhz = currentFrequencyMhz,
                maxFrequencyMhz = maxFrequencyMhz,
                implementer = implementer,
                architecture = architecture,
                variant = variant,
                part = part,
            )
            currentIndex = null
            modelName = null
            currentFrequencyMhz = null
            maxFrequencyMhz = null
            implementer = null
            architecture = null
            variant = null
            part = null
        }

        for (rawLine in content.lineSequence()) {
            val line = rawLine.trim()
            if (line.isEmpty()) {
                flushProcessor()
                continue
            }

            val separatorIndex = line.indexOf(':')
            if (separatorIndex <= 0) continue

            val key = line.substring(0, separatorIndex).trim()
            val value = line.substring(separatorIndex + 1).trim()

            when (key) {
                "processor" -> {
                    flushProcessor()
                    currentIndex = value.toIntOrNull()
                }
                "model name" -> modelName = value
                "cpu MHz" -> currentFrequencyMhz = value.toFloatOrNull()
                "BogoMIPS" -> if (maxFrequencyMhz == null) maxFrequencyMhz = value.toFloatOrNull()
                "Hardware" -> hardware = value
                "CPU implementer" -> implementer = value
                "CPU architecture" -> architecture = value
                "CPU variant" -> variant = value
                "CPU part" -> part = value
            }
        }
        flushProcessor()

        require(processors.isNotEmpty()) { "No processors found in /proc/cpuinfo" }

        return CpuArchitectureInfo(
            coreCount = processors.size,
            processors = processors,
            abi = abi,
            hardware = hardware,
        )
    }
}
