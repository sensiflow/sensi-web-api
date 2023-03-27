package com.isel.sensiflow.services.dto.input

import com.isel.sensiflow.Constants
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class DevicesGroupInputDTO(
    val deviceIDs: List<Int> = emptyList()
)

data class DevicesGroupUpdateDTO(
    @field:NotBlank
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
    val description: String? = null
)
