package com.cpumonitor.feature.deviceinfo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cpumonitor.core.designsystem.component.MonitorCard
import com.cpumonitor.core.designsystem.component.MonitorCardHeader
import com.cpumonitor.core.designsystem.theme.MonitorColors
import com.cpumonitor.core.designsystem.theme.MonitorDimens
import com.cpumonitor.core.ui.R
import com.cpumonitor.core.ui.state.UiState
import com.cpumonitor.domain.display.ScreenResolutionClassifier
import com.cpumonitor.domain.model.device.AuthenticityLevel
import com.cpumonitor.domain.model.device.AuthenticitySeverity
import com.cpumonitor.domain.model.device.CameraFacing
import com.cpumonitor.domain.model.device.CameraInfo
import com.cpumonitor.domain.model.device.CpuCoreSysfsInfo
import com.cpumonitor.domain.model.device.DeviceFeatureFlags
import com.cpumonitor.domain.model.device.DeviceSpec
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceInfoScreen(
    viewModel: DeviceInfoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.device_info_title)) },
                actions = {
                    TextButton(onClick = viewModel::refresh) {
                        Text(stringResource(R.string.device_info_refresh))
                    }
                },
            )
        },
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )

            is UiState.Error -> Text(
                text = state.message,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(MonitorDimens.spacingLg),
                color = MaterialTheme.colorScheme.error,
            )

            is UiState.Success -> DeviceInfoContent(
                data = state.data,
                contentPadding = padding,
            )
        }
    }
}

@Composable
private fun DeviceInfoContent(
    data: DeviceInfoUiData,
    contentPadding: PaddingValues,
) {
    val spec = data.spec
    val report = data.report
    val processor = spec.cpuArchitecture.processors.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = MonitorDimens.spacingLg + contentPadding.calculateLeftPadding(
                androidx.compose.ui.unit.LayoutDirection.Ltr,
            ),
            end = MonitorDimens.spacingLg + contentPadding.calculateRightPadding(
                androidx.compose.ui.unit.LayoutDirection.Ltr,
            ),
            top = MonitorDimens.spacingSm + contentPadding.calculateTopPadding(),
            bottom = MonitorDimens.spacingLg + contentPadding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingMd),
    ) {
        item {
            AuthenticityCard(
                level = report.level,
                scorePercent = report.scorePercent,
                summary = report.summary,
            )
        }

        if (report.flags.isNotEmpty()) {
            item {
                MonitorCard {
                    MonitorCardHeader(
                        title = stringResource(R.string.device_info_authenticity_checks),
                        subtitle = stringResource(R.string.device_info_authenticity_checks_subtitle),
                    )
                    Column(
                        modifier = Modifier.padding(top = MonitorDimens.spacingMd),
                        verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
                    ) {
                        report.flags.forEach { flag ->
                            AuthenticityFlagItem(
                                title = flag.title,
                                detail = flag.detail,
                                severity = flag.severity,
                            )
                        }
                    }
                }
            }
        }

        item {
            SpecSection(
                title = stringResource(R.string.device_info_identity),
                subtitle = stringResource(R.string.device_info_identity_subtitle),
                rows = listOf(
                    stringResource(R.string.device_info_manufacturer) to spec.buildInfo.manufacturer,
                    stringResource(R.string.device_info_brand) to spec.buildInfo.brand,
                    stringResource(R.string.device_info_model) to spec.buildInfo.model,
                    stringResource(R.string.device_info_device) to spec.buildInfo.device,
                    stringResource(R.string.device_info_product) to spec.buildInfo.product,
                    stringResource(R.string.device_info_board) to spec.buildInfo.board,
                    stringResource(R.string.device_info_hardware) to spec.buildInfo.hardware,
                    stringResource(R.string.device_info_bootloader) to spec.buildInfo.bootloader,
                    stringResource(R.string.device_info_fingerprint) to spec.buildInfo.fingerprint,
                ),
            )
        }

        item {
            SpecSection(
                title = stringResource(R.string.device_info_cpu),
                subtitle = stringResource(R.string.device_info_cpu_subtitle),
                rows = buildCpuRows(
                    spec = spec,
                    architecture = processor?.architecture,
                    implementer = processor?.implementer,
                    part = processor?.part,
                    variant = processor?.variant,
                ),
            )
        }

        if (spec.cpuCores.isNotEmpty()) {
            item {
                MonitorCard {
                    MonitorCardHeader(
                        title = stringResource(R.string.device_info_cpu_cores),
                        subtitle = stringResource(R.string.device_info_cpu_cores_subtitle),
                    )
                    Column(
                        modifier = Modifier.padding(top = MonitorDimens.spacingMd),
                        verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
                    ) {
                        spec.cpuCores.forEach { core ->
                            CpuCoreRow(core)
                        }
                    }
                }
            }
        }

        item {
            SpecSection(
                title = stringResource(R.string.device_info_memory_storage),
                subtitle = stringResource(R.string.device_info_memory_storage_subtitle),
                rows = listOf(
                    stringResource(R.string.device_info_total_ram) to formatBytes(spec.totalRamBytes),
                    stringResource(R.string.device_info_total_storage) to formatBytes(spec.totalStorageBytes),
                ),
            )
        }

        item {
            SpecSection(
                title = stringResource(R.string.device_info_software),
                subtitle = stringResource(R.string.device_info_software_subtitle),
                rows = listOf(
                    stringResource(R.string.device_info_android_version) to spec.softwareInfo.androidVersion,
                    stringResource(R.string.device_info_api_level) to spec.softwareInfo.apiLevel.toString(),
                    stringResource(R.string.device_info_security_patch) to spec.softwareInfo.securityPatch.ifBlank { "—" },
                    stringResource(R.string.device_info_kernel) to spec.softwareInfo.kernelVersion.ifBlank { "—" },
                    stringResource(R.string.device_info_build_id) to spec.softwareInfo.buildId,
                    stringResource(R.string.device_info_build_tags) to spec.softwareInfo.buildTags,
                    stringResource(R.string.device_info_build_type) to spec.softwareInfo.buildType,
                    stringResource(R.string.device_info_display_build) to spec.buildInfo.display,
                ),
            )
        }

        item {
            SpecSection(
                title = stringResource(R.string.device_info_screen),
                subtitle = stringResource(R.string.device_info_screen_subtitle),
                rows = listOf(
                    stringResource(R.string.device_info_resolution) to formatResolution(spec),
                    stringResource(R.string.device_info_density) to
                        "${spec.screenInfo.densityDpi} dpi (${spec.screenInfo.density}x)",
                    stringResource(R.string.device_info_refresh_rate) to
                        "${spec.screenInfo.refreshRateHz.roundToInt()} Hz",
                ),
            )
        }

        item {
            SpecSection(
                title = stringResource(R.string.device_info_gpu),
                subtitle = stringResource(R.string.device_info_gpu_subtitle),
                rows = listOf(
                    stringResource(R.string.device_info_gpu_renderer) to (spec.gpuInfo.renderer ?: "—"),
                    stringResource(R.string.device_info_gpu_vendor) to (spec.gpuInfo.vendor ?: "—"),
                    stringResource(R.string.device_info_opengl_es) to (spec.gpuInfo.openGlEsVersion ?: "—"),
                ),
            )
        }

        item {
            CamerasSection(cameras = spec.peripherals.cameras)
        }

        item {
            AudioSection(audio = spec.peripherals.audio)
        }

        item {
            SensorsSection(sensors = spec.peripherals.sensors)
        }

        item {
            SpecSection(
                title = stringResource(R.string.device_info_connectivity),
                subtitle = stringResource(R.string.device_info_connectivity_subtitle),
                rows = buildConnectivityRows(spec.peripherals.features),
            )
        }
    }
}

@Composable
private fun AuthenticityCard(
    level: AuthenticityLevel,
    scorePercent: Int,
    summary: String,
) {
    val (title, color) = when (level) {
        AuthenticityLevel.TRUSTED ->
            stringResource(R.string.device_info_auth_trusted) to MonitorColors.HealthScore
        AuthenticityLevel.WARNING ->
            stringResource(R.string.device_info_auth_warning) to MonitorColors.Thermal
        AuthenticityLevel.SUSPICIOUS ->
            stringResource(R.string.device_info_auth_suspicious) to MaterialTheme.colorScheme.error
    }

    MonitorCard {
        Column(modifier = Modifier.padding(MonitorDimens.spacingMd)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = color,
                )
                Text(
                    text = stringResource(R.string.device_info_auth_score, scorePercent),
                    style = MaterialTheme.typography.titleMedium,
                    color = color,
                )
            }
            Text(
                text = summary,
                modifier = Modifier.padding(top = MonitorDimens.spacingSm),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.device_info_auth_disclaimer),
                modifier = Modifier.padding(top = MonitorDimens.spacingSm),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AuthenticityFlagItem(
    title: String,
    detail: String,
    severity: AuthenticitySeverity,
) {
    val color = when (severity) {
        AuthenticitySeverity.INFO -> MaterialTheme.colorScheme.onSurfaceVariant
        AuthenticitySeverity.WARNING -> MonitorColors.Thermal
        AuthenticitySeverity.CRITICAL -> MaterialTheme.colorScheme.error
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color,
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SpecSection(
    title: String,
    subtitle: String,
    rows: List<Pair<String, String>>,
) {
    MonitorCard {
        MonitorCardHeader(title = title, subtitle = subtitle)
        Column(
            modifier = Modifier.padding(top = MonitorDimens.spacingMd),
            verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingXs),
        ) {
            rows.forEach { (label, value) ->
                SpecRow(label = label, value = value)
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f).padding(end = MonitorDimens.spacingSm),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun CpuCoreRow(core: CpuCoreSysfsInfo) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.device_info_core_label, core.index),
            style = MaterialTheme.typography.labelLarge,
        )
        SpecRow(
            label = stringResource(R.string.device_info_current_freq),
            value = core.currentFrequencyKhz?.let { formatMhz(it) } ?: "—",
        )
        SpecRow(
            label = stringResource(R.string.device_info_min_freq),
            value = core.minFrequencyKhz?.let { formatMhz(it) } ?: "—",
        )
        SpecRow(
            label = stringResource(R.string.device_info_max_freq),
            value = core.maxFrequencyKhz?.let { formatMhz(it) } ?: "—",
        )
        SpecRow(
            label = stringResource(R.string.device_info_governor),
            value = core.governor ?: "—",
        )
    }
}

@Composable
private fun buildCpuRows(
    spec: DeviceSpec,
    architecture: String?,
    implementer: String?,
    part: String?,
    variant: String?,
): List<Pair<String, String>> {
    val avgCurrent = spec.cpuCores.mapNotNull { it.currentFrequencyKhz }.average()
        .takeIf { !it.isNaN() }
        ?.div(1_000.0)
        ?.roundToInt()
    val avgMax = spec.cpuCores.mapNotNull { it.maxFrequencyKhz }.average()
        .takeIf { !it.isNaN() }
        ?.div(1_000.0)
        ?.roundToInt()

    return buildList {
        add(stringResource(R.string.cpu_hardware) to (spec.cpuArchitecture.hardware ?: "—"))
        add(stringResource(R.string.cpu_cores) to spec.cpuArchitecture.coreCount.toString())
        add(stringResource(R.string.cpu_abi) to spec.cpuArchitecture.abi)
        add(stringResource(R.string.device_info_supported_abis) to spec.buildInfo.supportedAbis.joinToString(", "))
        add(stringResource(R.string.cpu_arm_arch) to (architecture ?: "—"))
        add(stringResource(R.string.cpu_implementer) to (implementer ?: "—"))
        add(stringResource(R.string.cpu_part) to (part ?: "—"))
        add(stringResource(R.string.device_info_variant) to (variant ?: "—"))
        avgCurrent?.let { add(stringResource(R.string.device_info_avg_current_freq) to "$it MHz") }
        avgMax?.let { add(stringResource(R.string.device_info_avg_max_freq) to "$it MHz") }
        add(
            stringResource(R.string.device_info_cpu_usage_monitoring) to if (spec.cpuUsageMonitoringAvailable) {
                stringResource(R.string.device_info_available)
            } else {
                stringResource(R.string.device_info_blocked_by_rom)
            },
        )
    }
}

@Composable
private fun CamerasSection(cameras: List<CameraInfo>) {
    MonitorCard {
        MonitorCardHeader(
            title = stringResource(R.string.device_info_cameras),
            subtitle = stringResource(R.string.device_info_cameras_subtitle),
        )
        Column(
            modifier = Modifier.padding(top = MonitorDimens.spacingMd),
            verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
        ) {
            if (cameras.isEmpty()) {
                Text(
                    text = stringResource(R.string.device_info_no_cameras),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                cameras.forEach { camera ->
                    CameraRow(camera)
                }
            }
        }
    }
}

@Composable
private fun CameraRow(camera: CameraInfo) {
    val facing = stringResource(camera.facing.toLabelRes())
    val resolution = camera.maxResolution ?: "—"
    val megapixels = camera.megapixels?.let { stringResource(R.string.device_info_camera_mp, it) } ?: "—"
    val extras = buildList {
        if (camera.flashAvailable) add(stringResource(R.string.device_info_camera_flash))
        if (camera.opticalStabilization) add(stringResource(R.string.device_info_camera_ois))
        if (!camera.flashAvailable) add(stringResource(R.string.device_info_camera_no_flash))
    }.joinToString(", ")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingXs),
    ) {
        Text(
            text = stringResource(R.string.device_info_camera_summary, facing, megapixels),
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = "$resolution · $extras",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AudioSection(audio: com.cpumonitor.domain.model.device.AudioHardwareInfo) {
    MonitorCard {
        MonitorCardHeader(
            title = stringResource(R.string.device_info_audio),
            subtitle = stringResource(R.string.device_info_audio_subtitle),
        )
        Column(
            modifier = Modifier.padding(top = MonitorDimens.spacingMd),
            verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingXs),
        ) {
            SpecRow(
                label = stringResource(R.string.device_info_microphone),
                value = formatYesNo(audio.hasMicrophone),
            )
            SpecRow(
                label = stringResource(R.string.device_info_speakers),
                value = stringResource(R.string.device_info_audio_device_count, audio.outputDeviceCount),
            )
            if (audio.inputDevices.isNotEmpty()) {
                SpecRow(
                    label = stringResource(R.string.device_info_audio_input_devices),
                    value = audio.inputDevices.joinToString("\n"),
                )
            }
            if (audio.outputDevices.isNotEmpty()) {
                SpecRow(
                    label = stringResource(R.string.device_info_audio_output_devices),
                    value = audio.outputDevices.joinToString("\n"),
                )
            }
        }
    }
}

@Composable
private fun SensorsSection(sensors: List<com.cpumonitor.domain.model.device.SensorInfo>) {
    MonitorCard {
        MonitorCardHeader(
            title = stringResource(R.string.device_info_sensors),
            subtitle = stringResource(R.string.device_info_sensors_subtitle, sensors.size),
        )
        Column(
            modifier = Modifier.padding(top = MonitorDimens.spacingMd),
            verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingXs),
        ) {
            sensors.forEach { sensor ->
                Text(
                    text = stringResource(
                        R.string.device_info_sensor_entry,
                        sensor.typeLabel,
                        sensor.name,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun buildConnectivityRows(features: DeviceFeatureFlags): List<Pair<String, String>> =
    listOf(
        stringResource(R.string.device_info_bluetooth) to formatYesNo(features.bluetooth),
        stringResource(R.string.device_info_wifi) to formatYesNo(features.wifi),
        stringResource(R.string.device_info_nfc) to formatYesNo(features.nfc),
        stringResource(R.string.device_info_gps) to formatYesNo(features.gps),
        stringResource(R.string.device_info_telephony) to formatYesNo(features.telephony),
        stringResource(R.string.device_info_fingerprint_sensor) to formatYesNo(features.fingerprint),
        stringResource(R.string.device_info_face_unlock) to formatYesNo(features.faceBiometric),
        stringResource(R.string.device_info_usb_host) to formatYesNo(features.usbHost),
        stringResource(R.string.device_info_vibrator) to formatYesNo(features.vibrator),
        stringResource(R.string.device_info_accelerometer) to formatYesNo(features.accelerometer),
        stringResource(R.string.device_info_gyroscope) to formatYesNo(features.gyroscope),
        stringResource(R.string.device_info_compass) to formatYesNo(features.compass),
        stringResource(R.string.device_info_proximity) to formatYesNo(features.proximity),
        stringResource(R.string.device_info_light_sensor) to formatYesNo(features.light),
        stringResource(R.string.device_info_barometer) to formatYesNo(features.barometer),
        stringResource(R.string.device_info_step_counter) to formatYesNo(features.stepCounter),
    )

@Composable
private fun formatYesNo(enabled: Boolean): String =
    stringResource(if (enabled) R.string.device_info_yes else R.string.device_info_no)

@Composable
private fun CameraFacing.toLabelRes(): Int =
    when (this) {
        CameraFacing.BACK -> R.string.device_info_camera_facing_back
        CameraFacing.FRONT -> R.string.device_info_camera_facing_front
        CameraFacing.EXTERNAL -> R.string.device_info_camera_facing_external
        CameraFacing.UNKNOWN -> R.string.device_info_camera_facing_unknown
    }

@Composable
private fun formatResolution(spec: DeviceSpec): String {
    val width = spec.screenInfo.widthPixels
    val height = spec.screenInfo.heightPixels
    val resolutionClass = ScreenResolutionClassifier.classify(width, height)
    val classLabel = stringResource(resolutionClass.toLabelRes())
    return stringResource(R.string.device_info_resolution_value, width, height, classLabel)
}

@Composable
private fun ScreenResolutionClassifier.ResolutionClass.toLabelRes(): Int =
    when (this) {
        ScreenResolutionClassifier.ResolutionClass.SD -> R.string.screen_class_sd
        ScreenResolutionClassifier.ResolutionClass.HD -> R.string.screen_class_hd
        ScreenResolutionClassifier.ResolutionClass.FULL_HD -> R.string.screen_class_full_hd
        ScreenResolutionClassifier.ResolutionClass.QHD -> R.string.screen_class_qhd
        ScreenResolutionClassifier.ResolutionClass.UHD_4K -> R.string.screen_class_uhd_4k
        ScreenResolutionClassifier.ResolutionClass.UHD_8K -> R.string.screen_class_uhd_8k
        ScreenResolutionClassifier.ResolutionClass.UNKNOWN -> R.string.screen_class_unknown
    }

private fun formatMhz(frequencyKhz: Int): String =
    "${(frequencyKhz / 1_000.0).roundToInt()} MHz"

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024 && unitIndex < units.lastIndex) {
        value /= 1024
        unitIndex++
    }
    return if (unitIndex == 0) {
        "$bytes ${units[unitIndex]}"
    } else {
        "${"%.2f".format(value)} ${units[unitIndex]}"
    }
}
