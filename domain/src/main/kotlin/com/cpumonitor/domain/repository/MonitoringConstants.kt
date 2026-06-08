package com.cpumonitor.domain.repository

/**
 * Shared monitoring timing defaults aligned with dashboard refresh requirements.
 */
object MonitoringConstants {
    /** Default realtime refresh interval (1 second). */
    const val DEFAULT_REFRESH_INTERVAL_MS = 1_000L

    /**
     * Minimum allowed polling interval.
     * Intervals below this value may exceed the 2% CPU overhead budget.
     */
    const val MIN_REFRESH_INTERVAL_MS = 500L
}
