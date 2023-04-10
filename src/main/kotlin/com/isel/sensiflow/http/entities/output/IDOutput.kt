package com.isel.sensiflow.http.entities.output

import com.isel.sensiflow.services.ID

data class IDOutput(
    val id: Int
)

/**
 * Converts a [ID] to a [IDOutput]
 */
fun ID.toIDOutput(): IDOutput {
    return IDOutput(
        id = this
    )
}
