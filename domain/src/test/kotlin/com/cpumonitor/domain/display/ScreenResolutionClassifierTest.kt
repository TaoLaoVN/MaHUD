package com.cpumonitor.domain.display

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenResolutionClassifierTest {

    @Test
    fun `1080 x 2400 is Full HD`() {
        assertEquals(
            ScreenResolutionClassifier.ResolutionClass.FULL_HD,
            ScreenResolutionClassifier.classify(1080, 2400),
        )
    }

    @Test
    fun `1440 x 3200 is QHD`() {
        assertEquals(
            ScreenResolutionClassifier.ResolutionClass.QHD,
            ScreenResolutionClassifier.classify(1440, 3200),
        )
    }

    @Test
    fun `720 x 1280 is HD`() {
        assertEquals(
            ScreenResolutionClassifier.ResolutionClass.HD,
            ScreenResolutionClassifier.classify(720, 1280),
        )
    }

    @Test
    fun `order of width and height does not matter`() {
        assertEquals(
            ScreenResolutionClassifier.classify(1080, 2400),
            ScreenResolutionClassifier.classify(2400, 1080),
        )
    }
}
