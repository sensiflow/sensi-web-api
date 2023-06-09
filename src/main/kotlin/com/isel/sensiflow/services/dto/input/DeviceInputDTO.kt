package com.isel.sensiflow.services.dto.input

import com.isel.sensiflow.Constants
import com.isel.sensiflow.model.entities.Device
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * Device input data transfer object
 * @param name The device name
 * @param description The device description
 * @param streamURL The device stream url
 *
 */
data class DeviceInputDTO(
    @field:NotBlank(message = Constants.Error.DEVICE_NAME_EMPTY)
    @field:NotNull(message = Constants.Error.DEVICE_NAME_EMPTY)
    @field:Size(
        min = 1,
        max = Constants.Device.NAME_MAX_LENGTH,
        message = Constants.Error.DEVICE_NAME_INVALID_LENGTH
    )
    val name: String,

    @field:Size(
        min = 0,
        max = Constants.Device.DESCRIPTION_MAX_LENGTH,
        message = Constants.Error.DEVICE_DESCRIPTION_INVALID_LENGTH
    )
    val description: String?,

    @field:NotBlank(message = Constants.Error.DEVICE_STREAM_URL_EMPTY)
    @field:NotNull(message = Constants.Error.DEVICE_STREAM_URL_EMPTY)
    @field:Size(
        min = 1,
        max = Constants.Device.STREAM_URL_MAX_LENGTH,
        message = Constants.Error.DEVICE_STREAM_URL_INVALID_LENGTH
    )
    @field:Pattern(
        regexp = Constants.Device.STREAM_URL_REGEX,
        message = Constants.Error.DEVICE_STREAM_URL_INVALID
    )
    val streamURL: String,
)

/**
 * Device update data transfer object
 * @param name The device name
 * @param description The device description
 * @param streamURL The device stream url
 */
data class DeviceUpdateDTO(
    @field:Size(
        min = Constants.Device.NAME_MIN_LENGTH,
        max = Constants.Device.NAME_MAX_LENGTH,
        message = Constants.Error.DEVICE_NAME_INVALID_LENGTH
    )
    val name: String? = null,

    @field:Size(
        min = 0,
        max = Constants.Device.DESCRIPTION_MAX_LENGTH,
        message = Constants.Error.DEVICE_DESCRIPTION_INVALID_LENGTH
    )
    val description: String? = null,

    @field:Size(
        min = 1,
        max = Constants.Device.STREAM_URL_MAX_LENGTH,
        message = Constants.Error.DEVICE_STREAM_URL_INVALID_LENGTH
    )
    @field:Pattern(
        regexp = Constants.Device.STREAM_URL_REGEX,
        message = Constants.Error.DEVICE_STREAM_URL_INVALID
    )
    val streamURL: String? = null,
)

/**
 * Device state input data transfer object
 */
data class DeviceStateInputDTO(
    @field:NotBlank(message = Constants.Error.DEVICE_STATE_REQUIRED)
    @field:NotNull(message = Constants.Error.DEVICE_STATE_REQUIRED)
    val state: String
)

/**
 * Checks if the device is empty.
 *
 * A device is empty if all the fields are null
 *
 * TODO: Unit test
 *
 * @return true if the device is empty, false otherwise
 */
fun DeviceUpdateDTO.fieldsAreEmpty(): Boolean =
    this.name == null &&
        this.description == null &&
        this.streamURL == null

/**
 * Checks if the device is equal to the input
 *
 * TODO: Unit test
 */
fun Device.isTheSameAs(input: DeviceUpdateDTO): Boolean =
    this.name == input.name &&
        this.description == input.description &&
        this.streamURL == input.streamURL
