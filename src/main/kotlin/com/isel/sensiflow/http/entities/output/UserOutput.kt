package com.isel.sensiflow.http.entities.output

import com.isel.sensiflow.model.dao.User

data class UserOutput(
    val email: String,
    val firstName: String,
    val lastName: String,
)

fun User.toDTO(): UserOutput {
    val email = this.email
    check(email != null) { "Invalid user creation" }
    return UserOutput(
        email = email.email,
        firstName = this.firstName,
        lastName = this.lastName,
    )
}
