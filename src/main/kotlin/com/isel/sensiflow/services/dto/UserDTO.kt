package com.isel.sensiflow.services.dto

import com.isel.sensiflow.http.entities.output.UserOutput
import com.isel.sensiflow.services.Role
import com.isel.sensiflow.services.UserID

data class UserDTO(
    val id: UserID,
    val email: String,
    val role: Role,
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
        role = this.role.name,
        id = this.id
    )
}
