package com.isel.sensiflow.http.entities.output

import com.isel.sensiflow.services.dto.UserDTO

/**
 * Represents the output of a [UserDTO]
 */
data class UserOutput(
    val email: String,
    val firstName: String,
    val lastName: String,
)
//TODO id