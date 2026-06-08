package com.cpumonitor.service.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.LifecycleOwner
import com.cpumonitor.core.logging.Logger
import com.cpumonitor.domain.model.OverlayMetrics
import com.cpumonitor.service.overlay.ui.OverlayFloatingPanel

/**
 * Manages the SYSTEM_ALERT_WINDOW floating Compose overlay with gaming-mode rendering optimizations.
 */
class OverlayWindowManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private val composeOwners = OverlayComposeOwners(lifecycleOwner)
    private var composeView: ComposeView? = null
    private var gamingModeEnabled by mutableStateOf(false)
    private var metrics by mutableStateOf(DEFAULT_METRICS)

    fun show(initialGamingMode: Boolean) {
        runOnMain {
            if (composeView != null) {
                gamingModeEnabled = initialGamingMode
                return@runOnMain
            }

            gamingModeEnabled = initialGamingMode
            val view = ComposeView(context).apply {
                composeOwners.attachTo(this)
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    OverlayFloatingPanel(
                        metrics = metrics,
                        gamingMode = gamingModeEnabled,
                    )
                }
            }

            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT,
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = OVERLAY_MARGIN_X
                y = OVERLAY_MARGIN_Y
            }

            try {
                windowManager.addView(view, layoutParams)
                composeView = view
                Logger.d("Overlay window attached")
            } catch (exception: Exception) {
                Logger.e("Failed to attach overlay window", exception)
                view.disposeComposition()
            }
        }
    }

    fun updateMetrics(newMetrics: OverlayMetrics) {
        runOnMain {
            if (metrics != newMetrics) {
                metrics = newMetrics
            }
        }
    }

    fun updateGamingMode(enabled: Boolean) {
        runOnMain {
            gamingModeEnabled = enabled
        }
    }

    fun hide() {
        runOnMain {
            composeView?.let { view ->
                try {
                    windowManager.removeView(view)
                } catch (exception: Exception) {
                    Logger.e("Failed to remove overlay window", exception)
                } finally {
                    view.disposeComposition()
                    composeView = null
                }
            }
        }
    }

    private fun runOnMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }

    companion object {
        private const val OVERLAY_MARGIN_X = 24
        private const val OVERLAY_MARGIN_Y = 96

        private val DEFAULT_METRICS = OverlayMetrics(
            cpuUsagePercent = 0f,
            ramUsagePercent = 0f,
            temperatureCelsius = 0f,
            fps = 0,
        )
    }
}
