package com.cpumonitor.data.datasource.system

import org.junit.Assert.assertEquals
import org.junit.Test

class ThermalTemperatureNormalizerTest {

    @Test
    fun normalize_keepsCelsiusValues() {
        assertEquals(42.5f, ThermalTemperatureNormalizer.normalize(42.5f), 0.01f)
    }

    @Test
    fun normalize_convertsMillidegrees() {
        assertEquals(45.2f, ThermalTemperatureNormalizer.normalize(45_200f), 0.01f)
    }
}
