package com.isel.sensiflow.model.repository

import com.isel.sensiflow.model.entities.SessionToken
import com.isel.sensiflow.model.entities.User
import org.springframework.data.jpa.repository.JpaRepository

interface SessionTokenRepository : JpaRepository<SessionToken, String> {
    fun findByToken(token: String): SessionToken?

    fun findByUser(user: User): SessionToken?
}
