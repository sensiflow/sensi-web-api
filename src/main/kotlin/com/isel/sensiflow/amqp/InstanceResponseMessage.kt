package com.isel.sensiflow.amqp

import com.isel.sensiflow.model.dao.DeviceProcessingState
import com.isel.sensiflow.services.ServiceInternalException

/**
 * Response received from the instance manager.
 * @property device_id the [ID] of the device that sent the response.
 * @property state the new state of the device.
 * @property code the response code.
 * @property message the response message.
 */
data class InstanceResponseMessage(
    val device_id: Int,
    private val state: String,
    val code: Int,
    val message: String
) {
    val newState = DeviceProcessingState.fromString(state) ?: throw ServiceInternalException("Invalid received from instance manager: $state")
}
