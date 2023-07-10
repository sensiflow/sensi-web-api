package com.isel.sensiflow.amqp.message.input

/**
 * Response received from the instance manager.
 * @property device_id the [ID] of the device that sent the response.
 * @property action the executed by the scheduler .
 * @property code the response code.
 * @property message the response message.
 */
data class SchedulerNotificationResponseMessage(
    val device_ids: List<Int>,
    val action: String,
    val code: Int,
    val message: String
)
