package com.isel.sensiflow.model.repository

import com.isel.sensiflow.model.dao.Email
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface EmailRepository : JpaRepository<Email, String> {

    fun findByEmail(email: String): Email?
}