package com.cpumonitor.domain.usecase.benchmark

import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.benchmark.BenchmarkMode
import com.cpumonitor.domain.model.benchmark.BenchmarkProgress
import com.cpumonitor.domain.model.benchmark.BenchmarkResult
import com.cpumonitor.domain.model.benchmark.StressTestDuration
import com.cpumonitor.domain.model.benchmark.StressTestResult
import com.cpumonitor.domain.repository.BenchmarkRepository
import com.cpumonitor.domain.usecase.FlowUseCase
import com.cpumonitor.domain.usecase.NoParamsUseCase
import com.cpumonitor.domain.usecase.UseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBenchmarkProgressUseCase @Inject constructor(
    private val benchmarkRepository: BenchmarkRepository,
) : FlowUseCase<Unit, BenchmarkProgress>() {

    override fun execute(params: Unit): Flow<BenchmarkProgress> =
        benchmarkRepository.observeProgress()
}

class RunCpuBenchmarkUseCase @Inject constructor(
    private val benchmarkRepository: BenchmarkRepository,
) : UseCase<BenchmarkMode, BenchmarkResult>() {

    override suspend fun execute(params: BenchmarkMode): Result<BenchmarkResult> =
        benchmarkRepository.runCpuBenchmark(params)
}

class RunStressTestUseCase @Inject constructor(
    private val benchmarkRepository: BenchmarkRepository,
) : UseCase<StressTestDuration, StressTestResult>() {

    override suspend fun execute(params: StressTestDuration): Result<StressTestResult> =
        benchmarkRepository.runStressTest(params)
}

class CancelBenchmarkSessionUseCase @Inject constructor(
    private val benchmarkRepository: BenchmarkRepository,
) : NoParamsUseCase<Unit>() {

    override suspend fun execute(): Result<Unit> =
        benchmarkRepository.cancelSession()
}
