package com.cpumonitor.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * CPU Monitor brand and semantic colors shared across all theme variants.
 */
object MonitorColors {
    val CpuUsage = Color(0xFF4CAF50)
    val CpuFrequency = Color(0xFF2196F3)
    val MemoryUsage = Color(0xFF9C27B0)
    val BatteryLevel = Color(0xFFFFC107)
    val Thermal = Color(0xFFFF5722)
    val StorageUsage = Color(0xFF00BCD4)
    val HealthScore = Color(0xFF8BC34A)

    val ChartGrid = Color(0x1FFFFFFF)
    val ChartGridLight = Color(0x1F000000)

    val AmoledBackground = Color(0xFF000000)
    val AmoledSurface = Color(0xFF0A0A0A)
    val AmoledSurfaceVariant = Color(0xFF141414)
}

/**
 * Light theme palette tuned for monitoring dashboards.
 */
internal val LightPrimary = Color(0xFF006E2C)
internal val LightOnPrimary = Color(0xFFFFFFFF)
internal val LightPrimaryContainer = Color(0xFF9FF6A9)
internal val LightOnPrimaryContainer = Color(0xFF002108)
internal val LightSecondary = Color(0xFF006874)
internal val LightOnSecondary = Color(0xFFFFFFFF)
internal val LightSecondaryContainer = Color(0xFF9EEFFD)
internal val LightOnSecondaryContainer = Color(0xFF001F24)
internal val LightTertiary = Color(0xFF6750A4)
internal val LightOnTertiary = Color(0xFFFFFFFF)
internal val LightTertiaryContainer = Color(0xFFEADDFF)
internal val LightOnTertiaryContainer = Color(0xFF21005D)
internal val LightBackground = Color(0xFFF0F2EE)
internal val LightOnBackground = Color(0xFF191C19)
internal val LightSurface = Color(0xFFFFFFFF)
internal val LightOnSurface = Color(0xFF191C19)
internal val LightSurfaceVariant = Color(0xFFDEE5D9)
internal val LightOnSurfaceVariant = Color(0xFF424940)
internal val LightOutline = Color(0xFF727970)
internal val LightError = Color(0xFFBA1A1A)
internal val LightOnError = Color(0xFFFFFFFF)

/**
 * Dark theme palette with elevated contrast for metric readability.
 */
internal val DarkPrimary = Color(0xFF84DA90)
internal val DarkOnPrimary = Color(0xFF003913)
internal val DarkPrimaryContainer = Color(0xFF00531F)
internal val DarkOnPrimaryContainer = Color(0xFF9FF6A9)
internal val DarkSecondary = Color(0xFF82D3E0)
internal val DarkOnSecondary = Color(0xFF00363D)
internal val DarkSecondaryContainer = Color(0xFF004F58)
internal val DarkOnSecondaryContainer = Color(0xFF9EEFFD)
internal val DarkTertiary = Color(0xFFD0BCFF)
internal val DarkOnTertiary = Color(0xFF381E72)
internal val DarkTertiaryContainer = Color(0xFF4F378B)
internal val DarkOnTertiaryContainer = Color(0xFFEADDFF)
internal val DarkBackground = Color(0xFF101410)
internal val DarkOnBackground = Color(0xFFE1E3DD)
internal val DarkSurface = Color(0xFF101410)
internal val DarkOnSurface = Color(0xFFE1E3DD)
internal val DarkSurfaceVariant = Color(0xFF424940)
internal val DarkOnSurfaceVariant = Color(0xFFC2C9BD)
internal val DarkOutline = Color(0xFF8C9389)
internal val DarkError = Color(0xFFFFB4AB)
internal val DarkOnError = Color(0xFF690005)

/**
 * AMOLED theme uses true black surfaces to reduce power on OLED displays.
 */
internal val AmoledPrimary = DarkPrimary
internal val AmoledOnPrimary = DarkOnPrimary
internal val AmoledPrimaryContainer = Color(0xFF003915)
internal val AmoledOnPrimaryContainer = DarkOnPrimaryContainer
internal val AmoledSecondary = DarkSecondary
internal val AmoledOnSecondary = DarkOnSecondary
internal val AmoledSecondaryContainer = Color(0xFF003840)
internal val AmoledOnSecondaryContainer = DarkOnSecondaryContainer
internal val AmoledTertiary = DarkTertiary
internal val AmoledOnTertiary = DarkOnTertiary
internal val AmoledTertiaryContainer = Color(0xFF37265F)
internal val AmoledOnTertiaryContainer = DarkOnTertiaryContainer
internal val AmoledBackground = MonitorColors.AmoledBackground
internal val AmoledOnBackground = DarkOnBackground
internal val AmoledSurface = MonitorColors.AmoledSurface
internal val AmoledOnSurface = DarkOnSurface
internal val AmoledSurfaceVariant = MonitorColors.AmoledSurfaceVariant
internal val AmoledOnSurfaceVariant = DarkOnSurfaceVariant
internal val AmoledOutline = Color(0xFF3A3A3A)
internal val AmoledError = DarkError
internal val AmoledOnError = DarkOnError
