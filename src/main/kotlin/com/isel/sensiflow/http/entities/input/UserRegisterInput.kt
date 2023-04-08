package com.isel.sensiflow.http.entities.input

import com.isel.sensiflow.Constants
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

    @field:NotBlank(
        message = Constants.Error.EMAIL_EMPTY
    )
    @field:Email(
        message = Constants.Error.EMAIL_INVALID_FORMAT
    )
    @field:Size(
        max = Constants.InputValidation.EMAIL_MAX_LENGTH,
        message = Constants.Error.EMAIL_INVALID_LENGTH
    )
    val email: String,

    @field:NotBlank(
        message = Constants.Error.NAME_EMPTY
    )
    @field:Size(
        min = Constants.InputValidation.NAME_MIN_LENGTH,
        max = Constants.InputValidation.NAME_MAX_LENGTH
    )
    val firstName: String,

    @field:NotBlank(
        message = Constants.Error.NAME_EMPTY
    )
    @field:Size(
        min = Constants.InputValidation.NAME_MIN_LENGTH,
        max = Constants.InputValidation.NAME_MAX_LENGTH
    )
    val lastName: String,

    @field:NotBlank(
        message = Constants.Error.PASSWORD_EMPTY
    )
    @field:Pattern(
        regexp = Constants.InputValidation.PASSWORD_REGEX,
        message = Constants.Error.PASSWORD_REGEX_MISMATCH
    )
    @field:Size(
        min = Constants.InputValidation.PASSWORD_MIN_SIZE,
        max = Constants.InputValidation.PASSWORD_MAX_SIZE,
        message = Constants.Error.PASSWORD_INVALID_LENGTH
    )
    val password: String
)
