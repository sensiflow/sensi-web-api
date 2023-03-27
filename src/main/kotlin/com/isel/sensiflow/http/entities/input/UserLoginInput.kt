package com.isel.sensiflow.http.entities.input

import com.isel.sensiflow.Constants.InputValidation.PASSWORD_MAX_SIZE
import com.isel.sensiflow.Constants.InputValidation.PASSWORD_MIN_SIZE
import com.isel.sensiflow.Constants.InputValidation.PASSWORD_REGEX
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.web.bind.MethodArgumentNotValidException

/**
 * Represents the input of a user login request.
 * All fields are required.
 * All the fields are validated here.
 * @param email Email of the user
 * @param password Password of the user
 * @throws MethodArgumentNotValidException if any of the fields is not valid.
 */
data class UserLoginInput(
    @field:NotBlank()
    @field:Email()
    val email: String,

    @field:NotBlank
    @field:Pattern(regexp = PASSWORD_REGEX)
    @field:Size(min = PASSWORD_MIN_SIZE, max = PASSWORD_MAX_SIZE)
    val password: String
)
