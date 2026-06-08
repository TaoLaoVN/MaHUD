package com.cpumonitor.data.datasource.overlay

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android-specific overlay permission checks for SYSTEM_ALERT_WINDOW.
 */
@Singleton
class OverlayPermissionDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun isOverlayPermissionGranted(): Boolean =
        Settings.canDrawOverlays(context)
}
