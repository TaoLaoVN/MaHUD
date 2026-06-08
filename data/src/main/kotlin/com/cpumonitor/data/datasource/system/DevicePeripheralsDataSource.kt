package com.cpumonitor.data.datasource.system

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.fingerprint.FingerprintManager
import android.media.AudioDeviceInfo as AndroidAudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import com.cpumonitor.data.datasource.SystemDataSource
import com.cpumonitor.domain.model.device.AudioHardwareInfo
import com.cpumonitor.domain.model.device.CameraFacing
import com.cpumonitor.domain.model.device.CameraInfo
import com.cpumonitor.domain.model.device.DeviceFeatureFlags
import com.cpumonitor.domain.model.device.DevicePeripheralsInfo
import com.cpumonitor.domain.model.device.SensorInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Reads camera, audio, sensor, and system-feature metadata exposed without root.
 */
@Singleton
class DevicePeripheralsDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : SystemDataSource {

    fun readPeripheralsInfo(): DevicePeripheralsInfo =
        DevicePeripheralsInfo(
            cameras = readCameras(),
            audio = readAudioHardware(),
            sensors = readSensors(),
            features = readFeatureFlags(),
        )

    private fun readCameras(): List<CameraInfo> {
        return try {
            val cameraManager = context.getSystemService(CameraManager::class.java) ?: return emptyList()
            cameraManager.cameraIdList.mapNotNull { cameraId ->
                readCamera(cameraManager, cameraId)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun readCamera(cameraManager: CameraManager, cameraId: String): CameraInfo? {
        return try {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val facing = mapCameraFacing(characteristics.get(CameraCharacteristics.LENS_FACING))
            val configMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val largestJpeg = configMap
                ?.getOutputSizes(ImageFormat.JPEG)
                ?.maxByOrNull { it.width.toLong() * it.height }
            val megapixels = largestJpeg?.let { size ->
                ((size.width.toLong() * size.height) / 100_000f).roundToInt() / 10f
            }
            val resolution = largestJpeg?.let { "${it.width} x ${it.height}" }
            val flashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            val oisModes = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
            val opticalStabilization = oisModes?.any { it != CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_OFF } == true

            CameraInfo(
                cameraId = cameraId,
                facing = facing,
                megapixels = megapixels,
                maxResolution = resolution,
                flashAvailable = flashAvailable,
                opticalStabilization = opticalStabilization,
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun mapCameraFacing(lensFacing: Int?): CameraFacing =
        when (lensFacing) {
            CameraCharacteristics.LENS_FACING_FRONT -> CameraFacing.FRONT
            CameraCharacteristics.LENS_FACING_BACK -> CameraFacing.BACK
            CameraCharacteristics.LENS_FACING_EXTERNAL -> CameraFacing.EXTERNAL
            else -> CameraFacing.UNKNOWN
        }

    private fun readAudioHardware(): AudioHardwareInfo {
        val packageManager = context.packageManager
        val hasMicrophone = packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

        if (audioManager == null) {
            return AudioHardwareInfo(
                hasMicrophone = hasMicrophone,
                inputDeviceCount = 0,
                outputDeviceCount = 0,
                inputDevices = emptyList(),
                outputDevices = emptyList(),
            )
        }

        val inputs = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
            .map(::formatAudioDevice)
            .distinct()
        val outputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .map(::formatAudioDevice)
            .distinct()

        return AudioHardwareInfo(
            hasMicrophone = hasMicrophone || inputs.isNotEmpty(),
            inputDeviceCount = inputs.size,
            outputDeviceCount = outputs.size,
            inputDevices = inputs,
            outputDevices = outputs,
        )
    }

    private fun formatAudioDevice(device: AndroidAudioDeviceInfo): String {
        val typeLabel = audioDeviceTypeLabel(device.type)
        val name = device.productName?.toString()?.takeIf { it.isNotBlank() }
        return if (name.isNullOrBlank()) typeLabel else "$typeLabel ($name)"
    }

    private fun audioDeviceTypeLabel(type: Int): String =
        when (type) {
            AndroidAudioDeviceInfo.TYPE_BUILTIN_MIC -> "Built-in microphone"
            AndroidAudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Built-in speaker"
            AndroidAudioDeviceInfo.TYPE_WIRED_HEADSET -> "Wired headset"
            AndroidAudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Wired headphones"
            AndroidAudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth mic"
            AndroidAudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth speaker"
            AndroidAudioDeviceInfo.TYPE_USB_DEVICE -> "USB audio"
            AndroidAudioDeviceInfo.TYPE_USB_HEADSET -> "USB headset"
            AndroidAudioDeviceInfo.TYPE_TELEPHONY -> "Telephony"
            AndroidAudioDeviceInfo.TYPE_HDMI -> "HDMI audio"
            AndroidAudioDeviceInfo.TYPE_DOCK -> "Dock audio"
            AndroidAudioDeviceInfo.TYPE_FM -> "FM"
            AndroidAudioDeviceInfo.TYPE_AUX_LINE -> "Aux line"
            AndroidAudioDeviceInfo.TYPE_IP -> "IP audio"
            AndroidAudioDeviceInfo.TYPE_BUS -> "Bus audio"
            else -> "Audio device ($type)"
        }

    private fun readSensors(): List<SensorInfo> {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            ?: return emptyList()

        return sensorManager.getSensorList(Sensor.TYPE_ALL)
            .map { sensor ->
                SensorInfo(
                    name = sensor.name,
                    typeLabel = sensorTypeLabel(sensor.type),
                    vendor = sensor.vendor,
                    version = sensor.version,
                )
            }
            .sortedBy { it.typeLabel }
    }

    private fun sensorTypeLabel(type: Int): String =
        when (type) {
            Sensor.TYPE_ACCELEROMETER -> "Accelerometer"
            Sensor.TYPE_GYROSCOPE -> "Gyroscope"
            Sensor.TYPE_MAGNETIC_FIELD -> "Magnetometer"
            Sensor.TYPE_LIGHT -> "Light"
            Sensor.TYPE_PROXIMITY -> "Proximity"
            Sensor.TYPE_PRESSURE -> "Barometer"
            Sensor.TYPE_AMBIENT_TEMPERATURE -> "Ambient temperature"
            Sensor.TYPE_RELATIVE_HUMIDITY -> "Humidity"
            Sensor.TYPE_GRAVITY -> "Gravity"
            Sensor.TYPE_LINEAR_ACCELERATION -> "Linear acceleration"
            Sensor.TYPE_ROTATION_VECTOR -> "Rotation vector"
            Sensor.TYPE_STEP_COUNTER -> "Step counter"
            Sensor.TYPE_STEP_DETECTOR -> "Step detector"
            Sensor.TYPE_HEART_RATE -> "Heart rate"
            Sensor.TYPE_SIGNIFICANT_MOTION -> "Significant motion"
            Sensor.TYPE_GAME_ROTATION_VECTOR -> "Game rotation"
            Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> "Geomagnetic rotation"
            else -> "Sensor type $type"
        }

    private fun readFeatureFlags(): DeviceFeatureFlags {
        val packageManager = context.packageManager
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

        return DeviceFeatureFlags(
            bluetooth = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH),
            wifi = packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI),
            nfc = packageManager.hasSystemFeature(PackageManager.FEATURE_NFC),
            gps = packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS),
            telephony = packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY),
            fingerprint = hasFingerprintSupport(packageManager),
            iris = packageManager.hasSystemFeature(PackageManager.FEATURE_IRIS),
            faceBiometric = hasFaceBiometricSupport(),
            usbHost = packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST),
            vibrator = hasVibrator(),
            accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null,
            gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null,
            compass = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null,
            proximity = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null,
            light = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT) != null,
            barometer = sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE) != null,
            stepCounter = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null,
        )
    }

    @Suppress("DEPRECATION")
    private fun hasFingerprintSupport(packageManager: PackageManager): Boolean {
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) return true
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        val fingerprintManager = context.getSystemService(FingerprintManager::class.java) ?: return false
        return fingerprintManager.isHardwareDetected
    }

    private fun hasFaceBiometricSupport(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return context.packageManager.hasSystemFeature(PackageManager.FEATURE_FACE)
        }
        return false
    }

    private fun hasVibrator(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
                as? android.os.VibratorManager
            return vibratorManager?.defaultVibrator?.hasVibrator() == true
        }
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
        return vibrator?.hasVibrator() == true
    }
}
