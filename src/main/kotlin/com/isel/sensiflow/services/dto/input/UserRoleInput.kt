package com.isel.sensiflow.services.dto.input

import com.isel.sensiflow.Constants
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserRoleInput(
    @field:NotBlank
    @field:Size(
        min = Constants.Roles.ROLE_NAME_MIN_LENGTH,
        max = Constants.Roles.ROLE_NAME_MAX_LENGTH,
        message = Constants.Error.USER_ROLE_NAME_INVALID_LENGTH
    )
    val role: String
)
