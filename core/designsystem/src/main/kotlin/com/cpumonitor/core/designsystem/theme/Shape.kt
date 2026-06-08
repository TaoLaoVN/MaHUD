package com.cpumonitor.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 shape tokens for monitoring cards and surfaces.
 */
val MonitorShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

/** Default corner radius for [com.cpumonitor.core.designsystem.component.MonitorCard]. */
val MonitorCardShape = RoundedCornerShape(16.dp)

/** Compact metric tile shape for dashboard grid cells. */
val MonitorMetricShape = RoundedCornerShape(12.dp)
