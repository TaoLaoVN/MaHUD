package com.cpumonitor.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * Supported application theme modes.
 */
enum class AppThemeMode {
    Light,
    Dark,
    Amoled,
}

/**
 * Provides the active [AppThemeMode] to descendants.
 */
val LocalAppThemeMode = staticCompositionLocalOf { AppThemeMode.Light }

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = LightError,
    onError = LightOnError,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = DarkError,
    onError = DarkOnError,
)

private val AmoledColorScheme = darkColorScheme(
    primary = AmoledPrimary,
    onPrimary = AmoledOnPrimary,
    primaryContainer = AmoledPrimaryContainer,
    onPrimaryContainer = AmoledOnPrimaryContainer,
    secondary = AmoledSecondary,
    onSecondary = AmoledOnSecondary,
    secondaryContainer = AmoledSecondaryContainer,
    onSecondaryContainer = AmoledOnSecondaryContainer,
    tertiary = AmoledTertiary,
    onTertiary = AmoledOnTertiary,
    tertiaryContainer = AmoledTertiaryContainer,
    onTertiaryContainer = AmoledOnTertiaryContainer,
    background = AmoledBackground,
    onBackground = AmoledOnBackground,
    surface = AmoledSurface,
    onSurface = AmoledOnSurface,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = AmoledOnSurfaceVariant,
    outline = AmoledOutline,
    error = AmoledError,
    onError = AmoledOnError,
)

/**
 * Resolves the [ColorScheme] for the given [AppThemeMode].
 */
fun colorSchemeFor(themeMode: AppThemeMode): ColorScheme = when (themeMode) {
    AppThemeMode.Light -> LightColorScheme
    AppThemeMode.Dark -> DarkColorScheme
    AppThemeMode.Amoled -> AmoledColorScheme
}

/**
 * Root Material 3 theme for CPU Monitor.
 *
 * @param themeMode explicit theme selection; when null, follows system dark/light.
 * @param dynamicColor uses Material You dynamic colors on Android 12+ when true.
 */
@Composable
fun CPUMonitorTheme(
    themeMode: AppThemeMode? = null,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val resolvedMode = themeMode ?: if (systemDark) AppThemeMode.Dark else AppThemeMode.Light

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            when (resolvedMode) {
                AppThemeMode.Light -> dynamicLightColorScheme(context)
                AppThemeMode.Dark,
                AppThemeMode.Amoled,
                -> dynamicDarkColorScheme(context).let { dynamic ->
                    if (resolvedMode == AppThemeMode.Amoled) {
                        dynamic.copy(
                            background = AmoledBackground,
                            surface = AmoledSurface,
                            surfaceVariant = AmoledSurfaceVariant,
                        )
                    } else {
                        dynamic
                    }
                }
            }
        }
        else -> colorSchemeFor(resolvedMode)
    }

    CompositionLocalProvider(LocalAppThemeMode provides resolvedMode) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MonitorTypography,
            shapes = MonitorShapes,
            content = content,
        )
    }
}

/** Convenience overload for legacy boolean dark-theme API. */
@Composable
fun CPUMonitorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    CPUMonitorTheme(
        themeMode = if (darkTheme) AppThemeMode.Dark else AppThemeMode.Light,
        dynamicColor = dynamicColor,
        content = content,
    )
}

/** Returns true when the active theme is AMOLED (true black surfaces). */
@Composable
fun isAmoledTheme(): Boolean = LocalAppThemeMode.current == AppThemeMode.Amoled
