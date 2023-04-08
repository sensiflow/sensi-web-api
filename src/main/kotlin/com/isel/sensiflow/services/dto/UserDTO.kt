package com.isel.sensiflow.services.dto

import com.isel.sensiflow.http.entities.output.UserOutput

data class UserDTO(
    val email: String,
    val firstName: String,
    val lastName: String,
)

/**
 * Converts a [UserDTO] to a [UserOutput]
 */
fun UserDTO.toOutput(): UserOutput {
    return UserOutput(
        email = this.email,
        firstName = this.firstName,
        lastName = this.lastName,
    )
}
