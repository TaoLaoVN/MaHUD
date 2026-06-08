package com.cpumonitor.domain.model.device

enum class CameraFacing {
    FRONT,
    BACK,
    EXTERNAL,
    UNKNOWN,
}

data class CameraInfo(
    val cameraId: String,
    val facing: CameraFacing,
    val megapixels: Float?,
    val maxResolution: String?,
    val flashAvailable: Boolean,
    val opticalStabilization: Boolean,
)

data class AudioHardwareInfo(
    val hasMicrophone: Boolean,
    val inputDeviceCount: Int,
    val outputDeviceCount: Int,
    val inputDevices: List<String>,
    val outputDevices: List<String>,
)

data class SensorInfo(
    val name: String,
    val typeLabel: String,
    val vendor: String,
    val version: Int,
)

data class DeviceFeatureFlags(
    val bluetooth: Boolean,
    val wifi: Boolean,
    val nfc: Boolean,
    val gps: Boolean,
    val telephony: Boolean,
    val fingerprint: Boolean,
    val iris: Boolean,
    val faceBiometric: Boolean,
    val usbHost: Boolean,
    val vibrator: Boolean,
    val accelerometer: Boolean,
    val gyroscope: Boolean,
    val compass: Boolean,
    val proximity: Boolean,
    val light: Boolean,
    val barometer: Boolean,
    val stepCounter: Boolean,
)

data class DevicePeripheralsInfo(
    val cameras: List<CameraInfo>,
    val audio: AudioHardwareInfo,
    val sensors: List<SensorInfo>,
    val features: DeviceFeatureFlags,
)
