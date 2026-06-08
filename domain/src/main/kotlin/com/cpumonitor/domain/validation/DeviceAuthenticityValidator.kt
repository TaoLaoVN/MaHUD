package com.cpumonitor.domain.validation

import com.cpumonitor.domain.model.device.AuthenticityFlag
import com.cpumonitor.domain.model.device.AuthenticityLevel
import com.cpumonitor.domain.model.device.AuthenticityReport
import com.cpumonitor.domain.model.device.AuthenticitySeverity
import com.cpumonitor.domain.model.device.DeviceSpec

/**
 * Cross-validates hardware and software signals to surface spoofing or replacement risks.
 *
 * Heuristics are conservative: they flag inconsistencies but cannot prove authenticity alone.
 */
object DeviceAuthenticityValidator {

    private val emulatorTokens = listOf(
        "goldfish",
        "ranchu",
        "vbox",
        "qemu",
        "generic",
        "android_x86",
        "sdk_gphone",
        "google_sdk",
        "emulator",
        "genymotion",
        "nox",
        "bluestacks",
    )

    fun validate(spec: DeviceSpec): AuthenticityReport {
        val flags = buildList {
            addAll(checkEmulatorSignals(spec))
            addAll(checkHardwareConsistency(spec))
            addAll(checkCpuConsistency(spec))
            addAll(checkSoftwareConsistency(spec))
            addAll(checkResourceSanity(spec))
            addAll(checkPeripheralSanity(spec))
        }

        val penalty = flags.sumOf { flag ->
            when (flag.severity) {
                AuthenticitySeverity.INFO -> 0
                AuthenticitySeverity.WARNING -> 12
                AuthenticitySeverity.CRITICAL -> 28
            }.toInt()
        }
        val scorePercent = (100 - penalty).coerceIn(0, 100)
        val level = when {
            flags.any { it.severity == AuthenticitySeverity.CRITICAL } -> AuthenticityLevel.SUSPICIOUS
            flags.any { it.severity == AuthenticitySeverity.WARNING } -> AuthenticityLevel.WARNING
            else -> AuthenticityLevel.TRUSTED
        }

        val summary = when (level) {
            AuthenticityLevel.TRUSTED ->
                "Hardware and software signals are internally consistent."
            AuthenticityLevel.WARNING ->
                "Some specifications look unusual. Review flagged items before trusting this device."
            AuthenticityLevel.SUSPICIOUS ->
                "Multiple inconsistencies detected. This device may be emulated, modified, or mislabeled."
        }

        return AuthenticityReport(
            level = level,
            scorePercent = scorePercent,
            summary = summary,
            flags = flags,
        )
    }

    private fun checkEmulatorSignals(spec: DeviceSpec): List<AuthenticityFlag> {
        val flags = mutableListOf<AuthenticityFlag>()
        val haystack = listOf(
            spec.buildInfo.hardware,
            spec.buildInfo.product,
            spec.buildInfo.model,
            spec.buildInfo.brand,
            spec.buildInfo.manufacturer,
            spec.buildInfo.fingerprint,
            spec.buildInfo.board,
            spec.cpuArchitecture.hardware.orEmpty(),
        ).joinToString(" ").lowercase()

        val matched = emulatorTokens.filter { haystack.contains(it) }
        if (matched.isNotEmpty()) {
            flags += AuthenticityFlag(
                id = "emulator_signature",
                title = "Emulator signature detected",
                detail = "Found emulator markers: ${matched.distinct().joinToString(", ")}",
                severity = AuthenticitySeverity.CRITICAL,
            )
        }

        if (spec.buildInfo.isEmulator) {
            flags += AuthenticityFlag(
                id = "build_emulator_flag",
                title = "Build properties indicate emulator",
                detail = "System build fields match known emulator fingerprints.",
                severity = AuthenticitySeverity.CRITICAL,
            )
        }

        val primaryAbi = spec.cpuArchitecture.abi.lowercase()
        if (primaryAbi.contains("x86") && !haystack.contains("chromebook")) {
            flags += AuthenticityFlag(
                id = "x86_abi_phone",
                title = "x86 ABI on phone-class device",
                detail = "Primary ABI is $primaryAbi, which is uncommon on physical ARM phones.",
                severity = AuthenticitySeverity.WARNING,
            )
        }

        return flags
    }

    private fun checkHardwareConsistency(spec: DeviceSpec): List<AuthenticityFlag> {
        val flags = mutableListOf<AuthenticityFlag>()
        val buildHardware = spec.buildInfo.hardware.normalizeToken()
        val cpuHardware = spec.cpuArchitecture.hardware?.normalizeToken()

        if (!buildHardware.isNullOrBlank() && !cpuHardware.isNullOrBlank()) {
            val matches = buildHardware == cpuHardware ||
                buildHardware.contains(cpuHardware) ||
                cpuHardware.contains(buildHardware)
            if (!matches) {
                flags += AuthenticityFlag(
                    id = "hardware_mismatch",
                    title = "CPU hardware mismatch",
                    detail = "Build.HARDWARE=${spec.buildInfo.hardware} but cpuinfo Hardware=${spec.cpuArchitecture.hardware}",
                    severity = AuthenticitySeverity.WARNING,
                )
            }
        }

        if (spec.buildInfo.manufacturer.equals("unknown", ignoreCase = true) ||
            spec.buildInfo.brand.equals("generic", ignoreCase = true)
        ) {
            flags += AuthenticityFlag(
                id = "generic_identity",
                title = "Generic manufacturer identity",
                detail = "Manufacturer=${spec.buildInfo.manufacturer}, brand=${spec.buildInfo.brand}",
                severity = AuthenticitySeverity.WARNING,
            )
        }

        if (spec.buildInfo.fingerprint.count { it == '/' } < 4) {
            flags += AuthenticityFlag(
                id = "invalid_fingerprint",
                title = "Unusual build fingerprint format",
                detail = spec.buildInfo.fingerprint,
                severity = AuthenticitySeverity.WARNING,
            )
        }

        return flags
    }

    private fun checkCpuConsistency(spec: DeviceSpec): List<AuthenticityFlag> {
        val flags = mutableListOf<AuthenticityFlag>()
        val cpuInfoCores = spec.cpuArchitecture.coreCount
        val sysfsCores = spec.cpuCores.size
        val runtimeCores = Runtime.getRuntime().availableProcessors()

        if (cpuInfoCores > 0 && sysfsCores > 0 && cpuInfoCores != sysfsCores) {
            flags += AuthenticityFlag(
                id = "core_count_mismatch",
                title = "CPU core count mismatch",
                detail = "cpuinfo=$cpuInfoCores cores, sysfs=$sysfsCores cores, runtime=$runtimeCores",
                severity = AuthenticitySeverity.WARNING,
            )
        }

        val readableFrequencies = spec.cpuCores.count { core ->
            (core.maxFrequencyKhz ?: 0) > 0 || (core.currentFrequencyKhz ?: 0) > 0
        }
        if (spec.cpuCores.isNotEmpty() && readableFrequencies == 0) {
            flags += AuthenticityFlag(
                id = "cpu_frequency_unavailable",
                title = "CPU frequency data unavailable",
                detail = "Sysfs cpufreq nodes returned no readable frequencies for any core.",
                severity = AuthenticitySeverity.INFO,
            )
        }

        val distinctParts = spec.cpuArchitecture.processors
            .mapNotNull { it.part?.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
        if (distinctParts.size > 3) {
            flags += AuthenticityFlag(
                id = "unusual_cpu_parts",
                title = "Unusually diverse CPU part IDs",
                detail = "Detected parts: ${distinctParts.joinToString(", ")}",
                severity = AuthenticitySeverity.INFO,
            )
        }

        if (!spec.cpuUsageMonitoringAvailable) {
            flags += AuthenticityFlag(
                id = "proc_stat_restricted",
                title = "/proc/stat restricted by ROM",
                detail = "This ROM blocks system-wide CPU usage reads. Frequency and cpuinfo data may still be valid.",
                severity = AuthenticitySeverity.INFO,
            )
        }

        return flags
    }

    private fun checkSoftwareConsistency(spec: DeviceSpec): List<AuthenticityFlag> {
        val flags = mutableListOf<AuthenticityFlag>()
        val patch = spec.softwareInfo.securityPatch

        if (patch.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            val parts = patch.split("-").map { it.toInt() }
            val patchMillis = java.util.Calendar.getInstance().apply {
                set(parts[0], parts[1] - 1, parts[2], 0, 0, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
            if (patchMillis > spec.timestampMillis) {
                flags += AuthenticityFlag(
                    id = "future_security_patch",
                    title = "Security patch date is in the future",
                    detail = patch,
                    severity = AuthenticitySeverity.CRITICAL,
                )
            }
        } else if (patch.isNotBlank() && !patch.equals("unknown", ignoreCase = true)) {
            flags += AuthenticityFlag(
                id = "invalid_security_patch",
                title = "Unexpected security patch format",
                detail = patch,
                severity = AuthenticitySeverity.WARNING,
            )
        }

        if (spec.softwareInfo.buildTags.contains("test-keys", ignoreCase = true)) {
            flags += AuthenticityFlag(
                id = "test_keys_build",
                title = "Custom or test build keys",
                detail = "Build tags include test-keys, which often indicates unofficial firmware.",
                severity = AuthenticitySeverity.WARNING,
            )
        }

        return flags
    }

    private fun checkResourceSanity(spec: DeviceSpec): List<AuthenticityFlag> {
        val flags = mutableListOf<AuthenticityFlag>()
        val ramGb = spec.totalRamBytes / 1_073_741_824.0

        if (ramGb in 0.1..1.5) {
            flags += AuthenticityFlag(
                id = "very_low_ram",
                title = "Very low RAM for a modern smartphone",
                detail = "Reported RAM: ${"%.1f".format(ramGb)} GB",
                severity = AuthenticitySeverity.WARNING,
            )
        }

        if (spec.totalStorageBytes in 1..4_000_000_000L) {
            flags += AuthenticityFlag(
                id = "very_low_storage",
                title = "Very low internal storage",
                detail = "Reported storage: ${spec.totalStorageBytes / 1_073_741_824} GB",
                severity = AuthenticitySeverity.WARNING,
            )
        }

        if (spec.screenInfo.widthPixels > 0 && spec.screenInfo.heightPixels > 0) {
            val minSide = minOf(spec.screenInfo.widthPixels, spec.screenInfo.heightPixels)
            if (minSide < 480) {
                flags += AuthenticityFlag(
                    id = "very_low_resolution",
                    title = "Unusually low screen resolution",
                    detail = "${spec.screenInfo.widthPixels} x ${spec.screenInfo.heightPixels}",
                    severity = AuthenticitySeverity.WARNING,
                )
            }
        }

        return flags
    }

    private fun checkPeripheralSanity(spec: DeviceSpec): List<AuthenticityFlag> {
        val flags = mutableListOf<AuthenticityFlag>()
        val features = spec.peripherals.features

        if (features.telephony && spec.peripherals.cameras.isEmpty()) {
            flags += AuthenticityFlag(
                id = "missing_camera",
                title = "No cameras on a phone-class device",
                detail = "Telephony is supported but the system reports zero cameras.",
                severity = AuthenticitySeverity.WARNING,
            )
        }

        if (features.telephony && !spec.peripherals.audio.hasMicrophone) {
            flags += AuthenticityFlag(
                id = "missing_microphone",
                title = "No microphone on a phone-class device",
                detail = "Telephony is supported but no microphone was detected.",
                severity = AuthenticitySeverity.WARNING,
            )
        }

        if (features.telephony && spec.peripherals.audio.outputDeviceCount == 0) {
            flags += AuthenticityFlag(
                id = "missing_speaker",
                title = "No audio output devices detected",
                detail = "Telephony is supported but no speaker/output audio path was found.",
                severity = AuthenticitySeverity.WARNING,
            )
        }

        return flags
    }

    private fun String.normalizeToken(): String =
        lowercase().replace(Regex("[^a-z0-9]"), "")
}
