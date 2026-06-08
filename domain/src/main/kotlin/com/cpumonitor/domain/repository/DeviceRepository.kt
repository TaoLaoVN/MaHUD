package com.cpumonitor.domain.repository

import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.device.AuthenticityReport
import com.cpumonitor.domain.model.device.DeviceSpec

/**
 * Domain contract for static device specification and authenticity validation.
 */
interface DeviceRepository : Repository {

    /**
     * Performs a one-shot read of verifiable hardware and software specifications.
     */
    suspend fun getDeviceSpec(): Result<DeviceSpec>

    /**
     * Cross-validates collected specifications and returns an authenticity report.
     */
    suspend fun validateAuthenticity(): Result<AuthenticityReport>
}
