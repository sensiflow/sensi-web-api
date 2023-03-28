package com.isel.sensiflow.http.entities.input

import com.isel.sensiflow.Constants.InputValidation.EMAIL_MAX_LENGTH
import com.isel.sensiflow.Constants.InputValidation.NAME_MAX_LENGTH
import com.isel.sensiflow.Constants.InputValidation.NAME_MIN_LENGTH
import com.isel.sensiflow.Constants.InputValidation.PASSWORD_MAX_SIZE
import com.isel.sensiflow.Constants.InputValidation.PASSWORD_MIN_SIZE
import com.isel.sensiflow.Constants.InputValidation.PASSWORD_REGEX
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.web.bind.MethodArgumentNotValidException

/**
 * Represents the input of a user register request.
 * All fields are required.
 * All the fields are validated here.
 * @param email Email of the user
 * @param firstName First name of the user
 * @param lastName Last name of the user
 * @param password Password of the user
 * @throws MethodArgumentNotValidException if any of the fields is not valid.
 */
data class UserRegisterInput(
    @field:NotBlank()
    @field:Email()
    @field:Size(
        max = EMAIL_MAX_LENGTH
    )
    val email: String,

    @field:NotBlank
    @field:Size(
        min = NAME_MIN_LENGTH,
        max = NAME_MAX_LENGTH
    )
    val firstName: String,

    @field:NotBlank
    @field:Size(
        min = NAME_MIN_LENGTH,
        max = NAME_MAX_LENGTH
    )
    val lastName: String,

    @field:NotBlank
    @field:Pattern(regexp = PASSWORD_REGEX)
    @field:Size(
        min = PASSWORD_MIN_SIZE,
        max = PASSWORD_MAX_SIZE
    )
    val password: String
)
