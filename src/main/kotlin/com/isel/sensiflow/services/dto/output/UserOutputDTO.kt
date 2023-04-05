package com.isel.sensiflow.services.dto.output

import com.isel.sensiflow.model.dao.User

/**
 * Represents the output of a [User]
 */
data class UserOutputDTO(
    val email: String,
    val firstName: String,
    val lastName: String,
)

/**
 * Converts a [User] to a [UserOutputDTO]
 */
fun User.toDTO(): UserOutputDTO {
    val email = this.email
    println(this.email)
    require(email != null) { "Invalid user creation" }
    return UserOutputDTO(
        email = email.email,
        firstName = this.firstName,
        lastName = this.lastName,
    )
}
