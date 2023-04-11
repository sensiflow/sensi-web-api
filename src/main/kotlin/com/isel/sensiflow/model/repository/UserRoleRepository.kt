package com.isel.sensiflow.model.repository

import com.isel.sensiflow.model.dao.Userrole
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRoleRepository : JpaRepository<Userrole, Int> {
    fun findByRole(role: String): Optional<Userrole>
}
