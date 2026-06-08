package com.cpumonitor.domain.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionComparatorTest {

    @Test
    fun isNewerVersion_prefersVersionCodeWhenPresent() {
        assertTrue(
            VersionComparator.isNewerVersion(
                remoteVersionName = "0.1.0",
                remoteVersionCode = 2,
                currentVersionName = "0.2.0",
                currentVersionCode = 1,
            ),
        )
    }

    @Test
    fun isNewerVersion_comparesSemVerWhenVersionCodeMissing() {
        assertTrue(
            VersionComparator.isNewerVersion(
                remoteVersionName = "0.2.0",
                remoteVersionCode = null,
                currentVersionName = "0.1.0",
                currentVersionCode = 1,
            ),
        )
        assertFalse(
            VersionComparator.isNewerVersion(
                remoteVersionName = "0.1.0",
                remoteVersionCode = null,
                currentVersionName = "0.2.0",
                currentVersionCode = 2,
            ),
        )
    }

    @Test
    fun normalizeTag_stripsLeadingV() {
        assertTrue(VersionComparator.normalizeTag("v1.2.3") == "1.2.3")
    }
}
