package com.cpumonitor.domain.model

/**
 * Realtime frames-per-second reading for overlay and performance diagnostics.
 */
data class FpsMetrics(
    val timestampMillis: Long,
    val fps: Int,
    val source: FpsSource = FpsSource.CHOREOGRAPHER,
)

/**
 * Indicates how the FPS value was derived.
 */
enum class FpsSource {
    /** Sampled from [android.view.Choreographer] vsync callbacks. */
    CHOREOGRAPHER,

    /** Fallback to [android.view.Display.getRefreshRate]. */
    DISPLAY_REFRESH_RATE,

    /** No FPS data available on this device. */
    UNAVAILABLE,
}
