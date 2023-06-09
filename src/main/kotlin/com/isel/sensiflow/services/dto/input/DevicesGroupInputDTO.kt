package com.isel.sensiflow.services.dto.input

import com.isel.sensiflow.Constants
import com.isel.sensiflow.http.entities.validation.NotBlankNullable
import com.isel.sensiflow.services.ID
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class DevicesGroupInputDTO(
    val deviceIDs: List<ID>
)

data class DevicesGroupUpdateDTO(
    @field:NotBlankNullable
    @field:Size(
        min = Constants.Group.NAME_MIN_LENGTH,
        max = Constants.Group.NAME_MAX_LENGTH,
        message = Constants.Error.GROUP_NAME_INVALID_LENGTH
    )
    val name: String? = null,

    @field:Size(
        min = 0,
        max = Constants.Group.DESCRIPTION_MAX_LENGTH,
        message = Constants.Error.GROUP_DESCRIPTION_INVALID_LENGTH
    )
    val description: String? = null
)

data class DevicesGroupCreateDTO(
    @field:NotBlank
    @field:Size(
        min = Constants.Group.NAME_MIN_LENGTH,
        max = Constants.Group.NAME_MAX_LENGTH,
        message = Constants.Error.GROUP_NAME_INVALID_LENGTH
    )
    val name: String,

    @field:Size(
        min = 0,
        max = Constants.Device.DESCRIPTION_MAX_LENGTH,
        message = Constants.Error.GROUP_DESCRIPTION_INVALID_LENGTH
    )
    @field:NotBlankNullable
    val description: String? = null
)
