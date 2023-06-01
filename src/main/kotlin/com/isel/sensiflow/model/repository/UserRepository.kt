package com.isel.sensiflow.model.repository

import com.isel.sensiflow.model.dao.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Int>
