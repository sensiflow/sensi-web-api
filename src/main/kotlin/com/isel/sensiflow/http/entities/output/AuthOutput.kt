package com.isel.sensiflow.http.entities.output

import com.isel.sensiflow.services.ID

data class AuthOutput(
    val id: ID,
    val expiresIn: Long
)
