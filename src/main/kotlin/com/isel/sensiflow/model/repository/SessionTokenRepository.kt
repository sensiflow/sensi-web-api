package com.isel.sensiflow.model.repository

import com.isel.sensiflow.model.dao.SessionToken
import org.springframework.data.jpa.repository.JpaRepository

interface SessionTokenRepository : JpaRepository<SessionToken, String>
