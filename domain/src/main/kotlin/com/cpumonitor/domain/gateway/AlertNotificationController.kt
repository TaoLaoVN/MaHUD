package com.cpumonitor.domain.gateway

/**
 * Gateway for posting user-facing alert notifications.
 */
interface AlertNotificationController {

    fun showAlert(title: String, message: String, notificationId: Int)
}
