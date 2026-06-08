package com.cpumonitor.data.monitoring

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import android.view.Display
import android.view.WindowManager
import com.cpumonitor.core.monitoring.FpsMonitorProvider
import com.cpumonitor.domain.model.FpsMetrics
import com.cpumonitor.domain.model.FpsSource
import com.cpumonitor.domain.repository.MonitoringConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers

/**
 * Native FPS monitor using [Choreographer] vsync sampling with display refresh-rate fallback.
 */
@Singleton
class FpsMonitorProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : FpsMonitorProvider {

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun observeFps(intervalMs: Long): Flow<FpsMetrics> {
        val safeIntervalMs = max(intervalMs, MonitoringConstants.MIN_REFRESH_INTERVAL_MS)
        val fallbackFps = readDisplayRefreshRateFps()

        return callbackFlow {
            val choreographer = Choreographer.getInstance()
            var frameCount = 0
            var windowStartNanos = System.nanoTime()

            val frameCallback = object : Choreographer.FrameCallback {
                override fun doFrame(frameTimeNanos: Long) {
                    frameCount++
                    val elapsedNanos = System.nanoTime() - windowStartNanos
                    if (elapsedNanos >= safeIntervalMs * 1_000_000L) {
                        val fps = if (frameCount > 0) {
                            ((frameCount * 1_000_000_000L) / elapsedNanos).toInt().coerceAtLeast(0)
                        } else {
                            fallbackFps
                        }
                        val source = if (frameCount > 0) {
                            FpsSource.CHOREOGRAPHER
                        } else {
                            FpsSource.DISPLAY_REFRESH_RATE
                        }
                        trySend(
                            FpsMetrics(
                                timestampMillis = System.currentTimeMillis(),
                                fps = fps,
                                source = source,
                            ),
                        )
                        frameCount = 0
                        windowStartNanos = System.nanoTime()
                    }
                    choreographer.postFrameCallback(this)
                }
            }

            mainHandler.post { choreographer.postFrameCallback(frameCallback) }

            awaitClose {
                mainHandler.post {
                    runCatching { choreographer.removeFrameCallback(frameCallback) }
                }
            }
        }
            .onStart {
                emit(
                    FpsMetrics(
                        timestampMillis = System.currentTimeMillis(),
                        fps = fallbackFps,
                        source = if (fallbackFps > 0) {
                            FpsSource.DISPLAY_REFRESH_RATE
                        } else {
                            FpsSource.UNAVAILABLE
                        },
                    ),
                )
            }
            .map { metrics ->
                if (metrics.fps <= 0 && fallbackFps > 0) {
                    metrics.copy(fps = fallbackFps, source = FpsSource.DISPLAY_REFRESH_RATE)
                } else {
                    metrics
                }
            }
            .distinctUntilChanged { old, new -> old.fps to old.source == new.fps to new.source }
            .flowOn(Dispatchers.Main.immediate)
    }

    private fun readDisplayRefreshRateFps(): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        @Suppress("DEPRECATION")
        val display: Display? = windowManager.defaultDisplay
        val refreshRate = display?.refreshRate ?: 0f
        return refreshRate.roundToInt().coerceAtLeast(0)
    }
}
