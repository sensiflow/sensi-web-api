package com.isel.sensiflow

import com.isel.sensiflow.http.entities.input.UserRegisterInput
import com.isel.sensiflow.model.repository.EmailRepository
import com.isel.sensiflow.services.Role
import com.isel.sensiflow.services.UserService
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class DbInit(
    private val userService: UserService,
    private val emailRepository: EmailRepository,
) {

    /**
     * Creates a new admin user in the database if it doesn't exist yet
     */
    @PostConstruct
    private fun init() {
        val exists = emailRepository
            .findByEmail("admin@gmail.com")
            .isPresent

        if (exists) return

        userService.createUser(
            UserRegisterInput(
                email = "admin@gmail.com",
                password = "Admin123.",
                firstName = "Admin",
                lastName = "Admin"
            ),
            Role.ADMIN
        )
    }
}
