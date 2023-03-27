package com.isel.sensiflow.model.repository

import com.isel.sensiflow.model.dao.SessionToken
import com.isel.sensiflow.model.dao.User
import org.springframework.data.jpa.repository.JpaRepository

interface SessionTokenRepository : JpaRepository<SessionToken, String> {
    fun findByToken(token: String): SessionToken?

    fun findByUser(user: User): SessionToken?
}
