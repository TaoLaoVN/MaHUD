package com.cpumonitor.domain.usecase.device

import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.device.AuthenticityReport
import com.cpumonitor.domain.model.device.DeviceSpec
import com.cpumonitor.domain.repository.DeviceRepository
import com.cpumonitor.domain.usecase.NoParamsUseCase
import javax.inject.Inject

class GetDeviceSpecUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
) : NoParamsUseCase<DeviceSpec>() {

    override suspend fun execute(): Result<DeviceSpec> = deviceRepository.getDeviceSpec()
}

class ValidateDeviceAuthenticityUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
) : NoParamsUseCase<AuthenticityReport>() {

    override suspend fun execute(): Result<AuthenticityReport> = deviceRepository.validateAuthenticity()
}
