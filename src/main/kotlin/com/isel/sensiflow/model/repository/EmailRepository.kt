package com.isel.sensiflow.model.repository

import com.isel.sensiflow.model.entities.Email
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface EmailRepository : JpaRepository<Email, String> {

    fun findByEmail(email: String): Optional<Email>
}
