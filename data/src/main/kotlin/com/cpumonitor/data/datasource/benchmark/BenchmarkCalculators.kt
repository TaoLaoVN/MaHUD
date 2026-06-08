package com.cpumonitor.data.datasource.benchmark

import kotlin.math.sqrt

internal object BenchmarkCalculators {

    fun calculateScore(operations: Long, durationMillis: Long): Int {
        if (durationMillis <= 0L || operations <= 0L) return 0
        val opsPerSecond = operations.toDouble() / durationMillis.toDouble() * 1_000.0
        return (opsPerSecond / 10.0).toInt().coerceIn(0, 10_000)
    }

    fun calculateFrequencyStabilityPercent(samples: List<Float>): Float {
        if (samples.isEmpty()) return 0f
        if (samples.size == 1) return 100f

        val mean = samples.average().toFloat()
        if (mean <= 0f) return 0f

        val variance = samples.map { sample -> (sample - mean) * (sample - mean) }.average()
        val coefficientOfVariation = sqrt(variance).toFloat() / mean
        return ((1f - coefficientOfVariation.coerceIn(0f, 1f)) * 100f).coerceIn(0f, 100f)
    }

    fun averageTemperature(samples: List<Float>): Float =
        if (samples.isEmpty()) 0f else samples.average().toFloat()

    fun peakTemperature(samples: List<Float>): Float =
        samples.maxOrNull() ?: 0f
}
