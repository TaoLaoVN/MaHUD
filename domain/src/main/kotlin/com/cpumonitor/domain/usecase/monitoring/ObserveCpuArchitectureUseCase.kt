package com.cpumonitor.domain.usecase.monitoring

import com.cpumonitor.domain.model.CpuArchitectureInfo
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.repository.CpuRepository
import com.cpumonitor.domain.usecase.NoParamsFlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Streams CPU architecture and frequency information from the repository.
 */
class ObserveCpuArchitectureUseCase @Inject constructor(
    private val cpuRepository: CpuRepository,
) : NoParamsFlowUseCase<CpuArchitectureInfo>() {

    override fun execute(): Flow<CpuArchitectureInfo> =
        cpuRepository.observeCpuArchitecture().map(::unwrap)
}
