package com.cpumonitor.domain.validation

import com.cpumonitor.domain.model.CpuArchitectureInfo
import com.cpumonitor.domain.model.CpuProcessorInfo
import com.cpumonitor.domain.model.device.AuthenticityLevel
import com.cpumonitor.domain.model.device.AuthenticitySeverity
import com.cpumonitor.domain.model.device.CpuCoreSysfsInfo
import com.cpumonitor.domain.model.device.DeviceBuildInfo
import com.cpumonitor.domain.model.device.AudioHardwareInfo
import com.cpumonitor.domain.model.device.CameraFacing
import com.cpumonitor.domain.model.device.CameraInfo
import com.cpumonitor.domain.model.device.DeviceFeatureFlags
import com.cpumonitor.domain.model.device.DevicePeripheralsInfo
import com.cpumonitor.domain.model.device.DeviceScreenInfo
import com.cpumonitor.domain.model.device.DeviceSoftwareInfo
import com.cpumonitor.domain.model.device.DeviceSpec
import com.cpumonitor.domain.model.device.GpuInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceAuthenticityValidatorTest {

    @Test
    fun `trusted when build and cpuinfo are consistent`() {
        val report = DeviceAuthenticityValidator.validate(sampleSpec())

        assertEquals(AuthenticityLevel.TRUSTED, report.level)
        assertTrue(report.flags.none { it.severity == AuthenticitySeverity.CRITICAL })
    }

    @Test
    fun `suspicious when emulator signature is present`() {
        val spec = sampleSpec().copy(
            buildInfo = sampleSpec().buildInfo.copy(
                fingerprint = "generic/sdk/generic:10/QP1A/test-keys",
                hardware = "goldfish",
                isEmulator = true,
            ),
        )

        val report = DeviceAuthenticityValidator.validate(spec)

        assertEquals(AuthenticityLevel.SUSPICIOUS, report.level)
        assertTrue(report.flags.any { it.id == "emulator_signature" })
    }

    @Test
    fun `warning when hardware values mismatch`() {
        val spec = sampleSpec().copy(
            cpuArchitecture = sampleSpec().cpuArchitecture.copy(hardware = "totally-different-soc"),
        )

        val report = DeviceAuthenticityValidator.validate(spec)

        assertEquals(AuthenticityLevel.WARNING, report.level)
        assertTrue(report.flags.any { it.id == "hardware_mismatch" })
    }

    @Test
    fun `info flag when proc stat is restricted`() {
        val spec = sampleSpec().copy(cpuUsageMonitoringAvailable = false)

        val report = DeviceAuthenticityValidator.validate(spec)

        assertTrue(report.flags.any { it.severity == AuthenticitySeverity.INFO })
    }

    private fun sampleSpec(): DeviceSpec {
        val processor = CpuProcessorInfo(
            index = 0,
            modelName = "Test CPU",
            currentFrequencyMhz = 1800f,
            maxFrequencyMhz = 2400f,
            implementer = "0x51",
            architecture = "8",
            variant = "0xd",
            part = "0x805",
        )
        return DeviceSpec(
            timestampMillis = 1_715_000_000_000L,
            buildInfo = DeviceBuildInfo(
                manufacturer = "Honor",
                brand = "HONOR",
                model = "AAK-AN00",
                device = "HNAKK",
                product = "AAK-AN00",
                board = "lahaina",
                hardware = "qcom",
                bootloader = "unknown",
                display = "AAK-AN00 1.0.0",
                fingerprint = "HONOR/AAK-AN00/HNAKK:14/UKQ1.230924.001/user/release-keys",
                supportedAbis = listOf("arm64-v8a", "armeabi-v7a"),
                isEmulator = false,
            ),
            softwareInfo = DeviceSoftwareInfo(
                androidVersion = "14",
                apiLevel = 34,
                securityPatch = "2024-03-01",
                kernelVersion = "5.10.149",
                buildId = "UKQ1.230924.001",
                buildTags = "release-keys",
                buildType = "user",
            ),
            cpuArchitecture = CpuArchitectureInfo(
                coreCount = 8,
                processors = List(8) { processor.copy(index = it) },
                abi = "arm64-v8a",
                hardware = "qcom",
            ),
            cpuCores = List(8) { index ->
                CpuCoreSysfsInfo(
                    index = index,
                    currentFrequencyKhz = 1_800_000,
                    minFrequencyKhz = 300_000,
                    maxFrequencyKhz = 2_400_000,
                    governor = "walt",
                )
            },
            gpuInfo = GpuInfo(
                renderer = "Adreno (TM) 660",
                vendor = "Qualcomm",
                openGlEsVersion = "3.2",
            ),
            screenInfo = DeviceScreenInfo(
                widthPixels = 1080,
                heightPixels = 2400,
                densityDpi = 480,
                density = 3f,
                refreshRateHz = 120f,
                physicalWidthInches = 2.4f,
                physicalHeightInches = 5.3f,
            ),
            peripherals = DevicePeripheralsInfo(
                cameras = listOf(
                    CameraInfo(
                        cameraId = "0",
                        facing = CameraFacing.BACK,
                        megapixels = 50f,
                        maxResolution = "8192 x 6144",
                        flashAvailable = true,
                        opticalStabilization = true,
                    ),
                ),
                audio = AudioHardwareInfo(
                    hasMicrophone = true,
                    inputDeviceCount = 1,
                    outputDeviceCount = 1,
                    inputDevices = listOf("Built-in microphone"),
                    outputDevices = listOf("Built-in speaker"),
                ),
                sensors = emptyList(),
                features = DeviceFeatureFlags(
                    bluetooth = true,
                    wifi = true,
                    nfc = true,
                    gps = true,
                    telephony = true,
                    fingerprint = true,
                    iris = false,
                    faceBiometric = true,
                    usbHost = true,
                    vibrator = true,
                    accelerometer = true,
                    gyroscope = true,
                    compass = true,
                    proximity = true,
                    light = true,
                    barometer = false,
                    stepCounter = true,
                ),
            ),
            totalRamBytes = 8L * 1024 * 1024 * 1024,
            totalStorageBytes = 256L * 1024 * 1024 * 1024,
            cpuUsageMonitoringAvailable = true,
        )
    }
}
