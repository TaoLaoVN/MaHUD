package com.cpumonitor.data.datasource.benchmark

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BenchmarkCalculatorsTest {

    @Test
    fun calculateScore_returnsNormalizedValue() {
        assertEquals(100, BenchmarkCalculators.calculateScore(operations = 1_000L, durationMillis = 1_000L))
        assertEquals(0, BenchmarkCalculators.calculateScore(operations = 0L, durationMillis = 1_000L))
    }

    @Test
    fun calculateFrequencyStabilityPercent_returnsHighForStableSamples() {
        val stability = BenchmarkCalculators.calculateFrequencyStabilityPercent(
            listOf(1_800f, 1_810f, 1_790f, 1_805f),
        )
        assertTrue(stability > 95f)
    }

    @Test
    fun calculateFrequencyStabilityPercent_returnsLowerForVolatileSamples() {
        val stability = BenchmarkCalculators.calculateFrequencyStabilityPercent(
            listOf(500f, 1_800f, 900f, 1_700f),
        )
        assertTrue(stability < 80f)
    }

    @Test
    fun temperatureAggregates_useExpectedValues() {
        val samples = listOf(35f, 40f, 38f)
        assertEquals(40f, BenchmarkCalculators.peakTemperature(samples))
        assertEquals(37.666668f, BenchmarkCalculators.averageTemperature(samples), 0.01f)
    }
}
