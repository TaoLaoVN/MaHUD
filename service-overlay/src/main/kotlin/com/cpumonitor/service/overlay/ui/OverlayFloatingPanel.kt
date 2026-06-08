package com.cpumonitor.service.overlay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cpumonitor.domain.model.OverlayMetrics
import kotlin.math.roundToInt

private val OverlayBackground = Color(0xCC0D1117)
private val OverlayText = Color(0xFFE6EDF3)

/**
 * Lightweight floating overlay UI optimized for gaming mode (minimal recomposition and memory).
 */
@Composable
fun OverlayFloatingPanel(
    metrics: OverlayMetrics,
    gamingMode: Boolean,
    modifier: Modifier = Modifier,
) {
    val cornerRadius = if (gamingMode) 6.dp else 10.dp
    val padding = if (gamingMode) 6.dp else 10.dp
    val labelSize = if (gamingMode) 9.sp else 10.sp
    val valueSize = if (gamingMode) 11.sp else 13.sp
    val fontFamily = remember { FontFamily.Monospace }

    Column(
        modifier = modifier
            .background(OverlayBackground, RoundedCornerShape(cornerRadius))
            .padding(padding),
    ) {
        OverlayMetricRow(
            label = if (gamingMode) "C" else "CPU",
            value = "${metrics.cpuUsagePercent.roundToInt()}%",
            labelSize = labelSize,
            valueSize = valueSize,
            fontFamily = fontFamily,
        )
        OverlayMetricRow(
            label = if (gamingMode) "R" else "RAM",
            value = "${metrics.ramUsagePercent.roundToInt()}%",
            labelSize = labelSize,
            valueSize = valueSize,
            fontFamily = fontFamily,
        )
        OverlayMetricRow(
            label = if (gamingMode) "T" else "TEMP",
            value = "${metrics.temperatureCelsius.roundToInt()}°C",
            labelSize = labelSize,
            valueSize = valueSize,
            fontFamily = fontFamily,
        )
        OverlayMetricRow(
            label = "FPS",
            value = metrics.fps.toString(),
            labelSize = labelSize,
            valueSize = valueSize,
            fontFamily = fontFamily,
        )
    }
}

@Composable
private fun OverlayMetricRow(
    label: String,
    value: String,
    labelSize: androidx.compose.ui.unit.TextUnit,
    valueSize: androidx.compose.ui.unit.TextUnit,
    fontFamily: FontFamily,
) {
    BasicText(
        text = "$label $value",
        style = TextStyle(
            color = OverlayText,
            fontSize = valueSize,
            fontFamily = fontFamily,
            lineHeight = labelSize,
        ),
        maxLines = 1,
    )
}
