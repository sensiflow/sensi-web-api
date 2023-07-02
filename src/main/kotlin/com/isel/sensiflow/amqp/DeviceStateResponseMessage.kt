package com.isel.sensiflow.amqp

import com.isel.sensiflow.services.ServiceInternalException

/**
 * Response received from the instance manager.
 * @property device_id the [ID] of the device that sent the response.
 * @property action the new state of the device.
 * @property code the response code.
 * @property message the response message.
 */
data class DeviceStateResponseMessage(
    val device_id: Int,
    val action: String,
    val code: Int,
    val message: String
) {
    val newState = ProcessingAction.fromString(action)?.state
        ?: throw ServiceInternalException("Invalid received from instance manager: $action")
}


fun DeviceStateResponseMessage.isSuccessful(): Boolean = code / 1000 == 2

fun DeviceStateResponseMessage.isError(): Boolean = code / 1000 == 4

fun DeviceStateResponseMessage.isNotFound(): Boolean = code == 4004
