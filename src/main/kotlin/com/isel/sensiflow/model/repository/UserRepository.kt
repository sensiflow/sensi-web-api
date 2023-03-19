package com.isel.sensiflow.model.repository

import com.isel.sensiflow.model.entities.User
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : CrudRepository<User, Long>
