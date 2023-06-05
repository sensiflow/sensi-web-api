package com.isel.sensiflow.amqp

import com.isel.sensiflow.Constants
import com.isel.sensiflow.services.InvalidParameterException
import jakarta.validation.Validation
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * Represents a message sent to the AMQP queue.
 * Validates itself using a [jakarta.validation.Validator], @throws [InvalidParameterException] if this InstanceMessage is invalid.
 * @param action The [Action] to be performed.
 * @param device_id The ID of the device to be acted upon.
 * @param device_stream_url The stream URL of the device to be acted upon, this url must be a valid RTSP stream.
 */
data class InstanceMessage(

    val action: Action,

    @Size(
        min = 0,
        message = Constants.Error.DEVICE_ID_MUST_BE_POSITVE
    )
    val device_id: Int,

    @field:Pattern(
        regexp = Constants.Device.STREAM_URL_REGEX,
        message = Constants.Error.DEVICE_STREAM_URL_INVALID
    )
    val device_stream_url: String?
) {
    companion object {
        private val validator = Validation.buildDefaultValidatorFactory().validator
    }
    init {
        val constraintViolations = validator.validate(this)
        if (constraintViolations.isNotEmpty()) {
            throw InvalidParameterException(constraintViolations.joinToString { it.message })
        }
    }
}
