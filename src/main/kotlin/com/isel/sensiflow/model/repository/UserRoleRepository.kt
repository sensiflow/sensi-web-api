package com.isel.sensiflow.model.repository

import com.isel.sensiflow.model.entities.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRoleRepository : JpaRepository<UserRole, Int> {
    fun findByRole(role: String): Optional<UserRole>
}
