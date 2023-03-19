package com.isel.sensiflow.data.repository

import com.isel.sensiflow.data.entities.User
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : CrudRepository<User, Long>
