package com.isel.sensiflow.http.entities.input

import com.isel.sensiflow.Constants
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
 * The validation is done using the javax.validation.constraints annotations.
 * @param email Email of the user
 * @param password Password of the user
 * @throws MethodArgumentNotValidException if any of the fields is not valid.
 */
data class UserLoginInput(
    @field:NotBlank(
        message = Constants.Error.EMAIL_EMPTY
    )
    @field:Email(
        message = Constants.Error.EMAIL_INVALID_FORMAT
    )
    val email: String,

    @field:NotBlank(
        message = Constants.Error.PASSWORD_EMPTY
    )
    @field:Pattern(
        regexp = PASSWORD_REGEX,
        message = Constants.Error.PASSWORD_REGEX_MISMATCH
    )
    @field:Size(
        min = PASSWORD_MIN_SIZE,
        max = PASSWORD_MAX_SIZE,
        message = Constants.Error.PASSWORD_INVALID_LENGTH
    )
    val password: String
)
