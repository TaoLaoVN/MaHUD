package com.cpumonitor.data.datasource.system

import android.os.Build
import com.cpumonitor.data.datasource.SystemDataSource
import com.cpumonitor.domain.model.device.DeviceBuildInfo
import com.cpumonitor.domain.model.device.DeviceSoftwareInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads static device identity and software metadata from [android.os.Build].
 */
@Singleton
class BuildInfoDataSource @Inject constructor() : SystemDataSource {

    fun readBuildInfo(): DeviceBuildInfo {
        val fingerprint = Build.FINGERPRINT.orEmpty()
        val hardware = Build.HARDWARE.orEmpty()
        val product = Build.PRODUCT.orEmpty()
        val model = Build.MODEL.orEmpty()
        val manufacturer = Build.MANUFACTURER.orEmpty()
        val brand = Build.BRAND.orEmpty()

        return DeviceBuildInfo(
            manufacturer = manufacturer,
            brand = brand,
            model = model,
            device = Build.DEVICE.orEmpty(),
            product = product,
            board = Build.BOARD.orEmpty(),
            hardware = hardware,
            bootloader = Build.BOOTLOADER.orEmpty(),
            display = Build.DISPLAY.orEmpty(),
            fingerprint = fingerprint,
            supportedAbis = Build.SUPPORTED_ABIS.toList(),
            isEmulator = detectEmulator(
                fingerprint = fingerprint,
                hardware = hardware,
                product = product,
                model = model,
                manufacturer = manufacturer,
                brand = brand,
            ),
        )
    }

    fun readSoftwareInfo(): DeviceSoftwareInfo =
        DeviceSoftwareInfo(
            androidVersion = Build.VERSION.RELEASE.orEmpty(),
            apiLevel = Build.VERSION.SDK_INT,
            securityPatch = Build.VERSION.SECURITY_PATCH.orEmpty(),
            kernelVersion = System.getProperty("os.version").orEmpty(),
            buildId = Build.ID.orEmpty(),
            buildTags = Build.TAGS.orEmpty(),
            buildType = Build.TYPE.orEmpty(),
        )

    private fun detectEmulator(
        fingerprint: String,
        hardware: String,
        product: String,
        model: String,
        manufacturer: String,
        brand: String,
    ): Boolean {
        val haystack = listOf(fingerprint, hardware, product, model, manufacturer, brand)
            .joinToString(" ")
            .lowercase()
        val emulatorTokens = listOf(
            "generic",
            "unknown",
            "google_sdk",
            "emulator",
            "android sdk built for x86",
            "sdk_gphone",
            "goldfish",
            "ranchu",
            "vbox",
            "genymotion",
        )
        return emulatorTokens.any { haystack.contains(it) }
    }
}
