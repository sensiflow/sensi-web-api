package com.isel.sensiflow.http.entities.input

import com.isel.sensiflow.Constants
import com.isel.sensiflow.http.entities.validation.NotBlankNullable
import com.isel.sensiflow.model.dao.User
import com.isel.sensiflow.services.hashPassword
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.web.bind.MethodArgumentNotValidException

/**
 * Represents the input of a user update request.
 * All fields are required
 * @param password Password of the user
 * @param firstName First name of the user
 * @param lastName Last name of the user
 * @throws MethodArgumentNotValidException if any of the fields is not valid.
 */
data class UserUpdateInput(

    @field:NotBlankNullable(
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
    val password: String? = null,

    @field:NotBlankNullable(
        message = Constants.Error.NAME_EMPTY
    )
    @field:Size(
        min = Constants.InputValidation.NAME_MIN_LENGTH,
        max = Constants.InputValidation.NAME_MAX_LENGTH
    )
    val firstName: String? = null,

    @field:NotBlankNullable(
        message = Constants.Error.NAME_EMPTY
    )
    @field:Size(
        min = Constants.InputValidation.NAME_MIN_LENGTH,
        max = Constants.InputValidation.NAME_MAX_LENGTH
    )
    val lastName: String? = null
)

/**
 * Checks if all the fields are empty
 */
fun UserUpdateInput.fieldsAreEmpty(): Boolean =
    this.password == null &&
        this.firstName == null &&
        this.lastName == null

/**
 * Checks if the input is the same as the user
 */
fun User.isTheSameAS(other: UserUpdateInput): Boolean {
    if (other.password == null) return false

    val hashedInputPassword = hashPassword(other.password, this.passwordSalt)

    return this.passwordHash == hashedInputPassword &&
        this.firstName == other.firstName &&
        this.lastName == other.lastName
}
