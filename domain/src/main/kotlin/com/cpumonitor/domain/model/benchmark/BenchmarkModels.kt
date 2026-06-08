package com.cpumonitor.domain.model.benchmark

enum class BenchmarkMode(val displayName: String) {
    SINGLE_CORE("Single Core"),
    MULTI_CORE("Multi Core"),
}

enum class StressTestDuration(val minutes: Int, val displayName: String) {
    MINUTES_5(5, "5 minutes"),
    MINUTES_10(10, "10 minutes"),
    MINUTES_30(30, "30 minutes"),
    ;

    val durationMillis: Long
        get() = minutes * 60_000L
}

enum class BenchmarkSessionType {
    CPU_BENCHMARK,
    STRESS_TEST,
}

enum class BenchmarkSessionStatus {
    IDLE,
    RUNNING,
    COMPLETED,
    CANCELLED,
    FAILED,
}

data class BenchmarkProgress(
    val status: BenchmarkSessionStatus,
    val sessionType: BenchmarkSessionType? = null,
    val progressPercent: Float = 0f,
    val elapsedMillis: Long = 0L,
    val totalMillis: Long = 0L,
    val currentTemperatureCelsius: Float? = null,
    val currentFrequencyMhz: Float? = null,
    val message: String? = null,
) {
    companion object {
        fun idle(): BenchmarkProgress = BenchmarkProgress(status = BenchmarkSessionStatus.IDLE)
    }
}

data class BenchmarkResult(
    val mode: BenchmarkMode,
    val score: Int,
    val durationMillis: Long,
    val peakTemperatureCelsius: Float,
    val averageTemperatureCelsius: Float,
    val frequencyStabilityPercent: Float,
    val timestampMillis: Long,
)

data class StressTestResult(
    val duration: StressTestDuration,
    val durationMillis: Long,
    val peakTemperatureCelsius: Float,
    val averageTemperatureCelsius: Float,
    val frequencyStabilityPercent: Float,
    val averageCpuUsagePercent: Float,
    val timestampMillis: Long,
)
