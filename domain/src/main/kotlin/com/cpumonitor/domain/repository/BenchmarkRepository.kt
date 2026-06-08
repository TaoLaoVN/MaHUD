package com.cpumonitor.domain.repository

import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.benchmark.BenchmarkMode
import com.cpumonitor.domain.model.benchmark.BenchmarkProgress
import com.cpumonitor.domain.model.benchmark.BenchmarkResult
import com.cpumonitor.domain.model.benchmark.StressTestDuration
import com.cpumonitor.domain.model.benchmark.StressTestResult
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for CPU benchmark and stress test sessions.
 */
interface BenchmarkRepository : Repository {

    fun observeProgress(): Flow<BenchmarkProgress>

    suspend fun runCpuBenchmark(mode: BenchmarkMode): Result<BenchmarkResult>

    suspend fun runStressTest(duration: StressTestDuration): Result<StressTestResult>

    suspend fun cancelSession(): Result<Unit>
}
