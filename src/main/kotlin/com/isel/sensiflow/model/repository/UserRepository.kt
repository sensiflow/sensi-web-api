package com.isel.sensiflow.model.repository

import com.isel.sensiflow.model.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Int>{
    fun findByEmail(email: String): Optional<User>
}
