package com.isel.sensiflow.http.entities.output

import com.isel.sensiflow.model.entities.User
import com.isel.sensiflow.services.UserID

/**
 * Represents the output of a [User]
 */
data class UserOutput(
    val id: UserID,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String
)
