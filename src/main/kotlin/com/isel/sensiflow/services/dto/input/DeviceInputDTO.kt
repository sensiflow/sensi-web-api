package com.isel.sensiflow.services.dto.input

import com.isel.sensiflow.Constants
import com.isel.sensiflow.model.dao.Device
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.web.bind.MethodArgumentNotValidException

/**
 * Device input data transfer object
 * @param name The device name
 * @param description The device description
 * @param streamURL The device stream url
 *
 * @throws MethodArgumentNotValidException if the input constraints are not met
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

    @field:NotBlank
    @field:Max(
        value = Constants.Device.DESCRIPTION_MAX_LENGTH.toLong(),
        message = Constants.Error.DEVICE_DESCRIPTION_INVALID_LENGTH
    )
    val description: String?,

    @field:NotBlank(message = Constants.Error.DEVICE_STREAM_URL_EMPTY)
    @field:NotNull(message = Constants.Error.DEVICE_STREAM_URL_EMPTY)
    @field:Max(
        value = Constants.Device.STREAM_URL_MAX_LENGTH.toLong(),
        message = Constants.Error.DEVICE_STREAM_URL_INVALID_LENGTH
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

    @Size(
        min = 1,
        max = Constants.Device.STREAM_URL_MAX_LENGTH,
        message = Constants.Error.DEVICE_STREAM_URL_INVALID_LENGTH
    )
    val streamURL: String? = null,
)

/**
 * Checks if the device is empty.
 *
 * A device is empty if all the fields are null
 *
 * @return true if the device is empty, false otherwise
 */
fun DeviceUpdateDTO.isEmpty(): Boolean =
    this.name == null &&
        this.description == null &&
        this.streamURL == null

/**
 * Checks if the device is equal to the input
 */
fun Device.isEqual(input: DeviceUpdateDTO): Boolean =
    this.name == input.name &&
        this.description == input.description &&
        this.streamURL == input.streamURL
