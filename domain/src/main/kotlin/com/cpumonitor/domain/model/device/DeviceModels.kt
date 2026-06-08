package com.cpumonitor.domain.model.device

import com.cpumonitor.domain.model.CpuArchitectureInfo

/**
 * Static identity fields from [android.os.Build] and related system properties.
 */
data class DeviceBuildInfo(
    val manufacturer: String,
    val brand: String,
    val model: String,
    val device: String,
    val product: String,
    val board: String,
    val hardware: String,
    val bootloader: String,
    val display: String,
    val fingerprint: String,
    val supportedAbis: List<String>,
    val isEmulator: Boolean,
)

/**
 * Software stack information exposed without root.
 */
data class DeviceSoftwareInfo(
    val androidVersion: String,
    val apiLevel: Int,
    val securityPatch: String,
    val kernelVersion: String,
    val buildId: String,
    val buildTags: String,
    val buildType: String,
)

/**
 * Display characteristics from [android.util.DisplayMetrics] and [android.view.Display].
 */
data class DeviceScreenInfo(
    val widthPixels: Int,
    val heightPixels: Int,
    val densityDpi: Int,
    val density: Float,
    val refreshRateHz: Float,
    val physicalWidthInches: Float?,
    val physicalHeightInches: Float?,
)

/**
 * Per-core CPU frequency data from sysfs cpufreq nodes.
 */
data class CpuCoreSysfsInfo(
    val index: Int,
    val currentFrequencyKhz: Int?,
    val minFrequencyKhz: Int?,
    val maxFrequencyKhz: Int?,
    val governor: String?,
)

/**
 * GPU information gathered from sysfs and GLES configuration when available.
 */
data class GpuInfo(
    val renderer: String?,
    val vendor: String?,
    val openGlEsVersion: String?,
)

/**
 * Aggregate snapshot of verifiable hardware and software specifications.
 */
data class DeviceSpec(
    val timestampMillis: Long,
    val buildInfo: DeviceBuildInfo,
    val softwareInfo: DeviceSoftwareInfo,
    val cpuArchitecture: CpuArchitectureInfo,
    val cpuCores: List<CpuCoreSysfsInfo>,
    val gpuInfo: GpuInfo,
    val screenInfo: DeviceScreenInfo,
    val peripherals: DevicePeripheralsInfo,
    val totalRamBytes: Long,
    val totalStorageBytes: Long,
    val cpuUsageMonitoringAvailable: Boolean,
)
