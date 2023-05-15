package com.isel.sensiflow.amqp

import com.isel.sensiflow.services.ID

/**
 * Response received from the instance manager.
 * @property device_id the [ID] of the device that sent the response.
 */
data class DeleteDeviceMessage(
    val device_id: ID
)
