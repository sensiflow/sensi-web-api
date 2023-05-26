package com.isel.sensiflow.services

import com.isel.sensiflow.Constants.User.SESSION_EXPIRATION_TIME
import com.isel.sensiflow.http.entities.input.UserLoginInput
import com.isel.sensiflow.http.entities.input.UserRegisterInput
import com.isel.sensiflow.http.entities.input.UserUpdateInput
import com.isel.sensiflow.http.entities.input.fieldsAreEmpty
import com.isel.sensiflow.http.entities.input.isTheSameAS
import com.isel.sensiflow.http.entities.output.UserOutput
import com.isel.sensiflow.model.dao.Email
import com.isel.sensiflow.model.dao.SessionToken
import com.isel.sensiflow.model.dao.User
import com.isel.sensiflow.model.dao.addEmail
import com.isel.sensiflow.model.dao.hasExpired
import com.isel.sensiflow.model.dao.toDTO
import com.isel.sensiflow.model.repository.EmailRepository
import com.isel.sensiflow.model.repository.SessionTokenRepository
import com.isel.sensiflow.model.repository.UserRepository
import com.isel.sensiflow.model.repository.UserRoleRepository
import com.isel.sensiflow.services.dto.AuthInformationDTO
import com.isel.sensiflow.services.dto.UserDTO
import com.isel.sensiflow.services.dto.input.UserRoleInput
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(noRollbackFor = [InvalidTokenException::class])
class UserService(
    private val userRepository: UserRepository,
    private val sessionTokenRepository: SessionTokenRepository,
    private val emailRepository: EmailRepository,
    private val userRoleRepository: UserRoleRepository
) {

    /**
     * Creates a new user in the database and a session token for it
     * @param userInput the user's information
     * @return the user's id and session token
     * @throws EmailAlreadyExistsException if the email already exists
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun createUser(userInput: UserRegisterInput, role: Role = Role.USER): UserID {
        emailRepository
            .findByEmail(userInput.email)
            .ifPresent {
                throw EmailAlreadyExistsException(userInput.email)
            }

        val salt = generateSalt()
        val hashedPassword = hashPassword(userInput.password, salt)

        val userRole = userRoleRepository.findByRole(role.name)
            .orElseThrow {
                throw RoleNotFoundException(role.name)
            }

        val user = userRepository.save(
            User(
                firstName = userInput.firstName,
                lastName = userInput.lastName,
                role = userRole,
                passwordHash = hashedPassword,
                passwordSalt = salt
            )
        )

        val email = emailRepository.save(
            Email(
                email = userInput.email,
                user = user
            )
        )

        userRepository.save(user.addEmail(email))

        return user.id
    }

    /**
     * Gets a user from the database identified by its [UserID] and returns it as a [UserOutput]
     * @param userID the user's id to be searched
     * @throws UserNotFoundException if the [User] is not found
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun getUser(userID: UserID): UserDTO {
        val user = userRepository
            .findById(userID)
            .orElseThrow {
                UserNotFoundException(userID)
            }
        return user.toDTO()
    }

    /**
     * Invalidates a session token, removing it from the database
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun invalidateSessionToken(token: String) {
        sessionTokenRepository
            .findByToken(token)
            ?.let {
                sessionTokenRepository.delete(it)
            }
    }

    /**
     * Verifies if the session token is valid and returns the user's id if it is
     * @param token the session token to be validated
     * @throws InvalidTokenException if the token is invalid or has expired
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun validateSessionToken(token: String): UserID {
        val sessionToken = sessionTokenRepository
            .findByToken(token) ?: throw InvalidTokenException("Invalid token")

        if (sessionToken.hasExpired()) {
            sessionTokenRepository.delete(sessionToken)
            throw InvalidTokenException("The received token has expired")
        }

        return sessionToken.user.id
    }

    /**
     * Confirms the user's login credentials
     * Reuses the session token if it is still valid otherwise creates a new one
     * @param userInput the user's login credentials
     * @returns a [AuthInformationDTO] object containing a session token and the user's id
     * @throws EmailNotFoundException if the email is not found
     * @throws InvalidCredentialsException if the password is invalid
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun authenticateUser(userInput: UserLoginInput): AuthInformationDTO {
        val email = emailRepository.findByEmail(userInput.email)
            .ifNotPresent {
                throw EmailNotFoundException(userInput.email)
            }
        requireNotNull(email)

        val user = email.user

        if (hashPassword(userInput.password, user.passwordSalt) != user.passwordHash) {
            throw InvalidCredentialsException("Invalid password")
        }

        val token = sessionTokenRepository.findByUser(user)

        if (token != null) {
            sessionTokenRepository.delete(token)
        }
        val sessionToken = sessionTokenRepository.save(
            SessionToken(
                user = user,
                token = generateUUID(),
                expiration = generateExpirationDate(SESSION_EXPIRATION_TIME).toTimeStamp()
            )
        )
        return AuthInformationDTO(sessionToken.token, user.id)
    }

    /**
     * Updates the user's role with the one provided in the [UserRoleInput]
     * @param userID the user's id to be updated
     * @param input the [UserRoleInput] containing the new role
     * @throws UserNotFoundException if the user is not found
     */
    fun updateRole(userID: UserID, input: UserRoleInput) {
        val user = userRepository.findById(userID)
            .orElseThrow { UserNotFoundException(userID) }

        userRepository.save(
            User(
                id = user.id,
                firstName = user.firstName,
                lastName = user.lastName,
                role = user.role,
                passwordHash = user.passwordHash,
                passwordSalt = user.passwordSalt
            )
        )
    }

    /**
     * Updates the user's information
     *
     * @param userID the user's id to be updated
     * @param invokerUserID the user's id that invoked the action
     * @param userInput the user's information containing:
     * a new password, if it is not provided the password will not be updated;
     * a new first name, if it is not provided the first name will not be updated;
     * a new last name, if it is not provided the last name will not be updated;
     * @throws UserNotFoundException if the user is not found
     */
    fun updateUser(
        userID: UserID,
        invokerUserID: UserID,
        userInput: UserUpdateInput
    ) {
        if (invokerUserID != userID) throw ActionForbiddenException("You can only update your own user")

        val user = userRepository.findById(userID)
            .orElseThrow { UserNotFoundException(userID) }

        if (userInput.fieldsAreEmpty() || user.isTheSameAS(userInput)) {
            return
        }

        val updatedUser = User(
            id = user.id,
            firstName = userInput.firstName ?: user.firstName,
            lastName = userInput.lastName ?: user.lastName,
            passwordHash = userInput.password?.let {
                hashPassword(it, user.passwordSalt)
            } ?: user.passwordHash,
            passwordSalt = user.passwordSalt,
            role = user.role
        ).addEmail(user.email)

        userRepository.save(updatedUser)
    }
}
