package com.isel.sensiflow.services

import com.isel.sensiflow.Constants.User.SESSION_EXPIRATION_TIME
import com.isel.sensiflow.http.entities.input.UserLoginInput
import com.isel.sensiflow.http.entities.input.UserRegisterInput
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
import com.isel.sensiflow.services.dto.AuthInformationDTO
import com.isel.sensiflow.services.dto.UserDTO
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(noRollbackFor = [InvalidTokenException::class])
class UserService(
    private val userRepository: UserRepository,
    private val sessionTokenRepository: SessionTokenRepository,
    private val emailRepository: EmailRepository
) {

    /**
     * Creates a new user in the database and a session token for it
     * @param userInput the user's information
     * @return the user's id and session token
     * @throws EmailAlreadyExistsException if the email already exists
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun createUser(userInput: UserRegisterInput): AuthInformationDTO {
        emailRepository
            .findByEmail(userInput.email)
            .ifPresent {
                throw EmailAlreadyExistsException(userInput.email)
            }

        val salt = generateSalt()
        val hashedPassword = hashPassword(userInput.password, salt)

        val user = userRepository.save(
            User(
                firstName = userInput.firstName,
                lastName = userInput.lastName,
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

        val persistedUser = userRepository.save(user.addEmail(email))
        val sessionToken = sessionTokenRepository.save(
            SessionToken(
                user = persistedUser,
                token = generateUUID(),
                expiration = generateExpirationDate(SESSION_EXPIRATION_TIME).toTimeStamp()
            )
        )

        return AuthInformationDTO(sessionToken.token, user.id)
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
            if (token.hasExpired()) {
                sessionTokenRepository.delete(token)
                val sessionToken = sessionTokenRepository.save(
                    SessionToken(
                        user = user,
                        token = generateUUID(),
                        expiration = generateExpirationDate(SESSION_EXPIRATION_TIME).toTimeStamp()
                    )
                )
                return AuthInformationDTO(sessionToken.token, user.id)
            } else {
                val timeUntilExpire = token.expiration
                    .toInstant()
                    .toMillis()
                    .minus(System.currentTimeMillis())

                return AuthInformationDTO(token.token, user.id, timeUntilExpire)
            }
        } else {
            val sessionToken = sessionTokenRepository.save(
                SessionToken(
                    user = user,
                    token = generateUUID(),
                    expiration = generateExpirationDate(SESSION_EXPIRATION_TIME).toTimeStamp()
                )
            )
            return AuthInformationDTO(sessionToken.token, user.id)
        }
    }
}
