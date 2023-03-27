package com.isel.sensiflow.services

import com.isel.sensiflow.Constants.User.SESSION_EXPIRATION_TIME
import com.isel.sensiflow.http.entities.input.UserLoginInput
import com.isel.sensiflow.http.entities.input.UserRegisterInput
import com.isel.sensiflow.http.entities.output.UserOutput
import com.isel.sensiflow.model.dao.Email
import com.isel.sensiflow.model.dao.SessionToken
import com.isel.sensiflow.model.dao.User
import com.isel.sensiflow.model.dao.hasExpired
import com.isel.sensiflow.model.repository.EmailRepository
import com.isel.sensiflow.model.repository.SessionTokenRepository
import com.isel.sensiflow.model.repository.UserRepository
import com.isel.sensiflow.services.dto.AuthInformationDTO
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
// @Transactional(noRollbackFor = [Exception::class])
class UserService(
    private val userRepository: UserRepository,
    private val sessionTokenRepository: SessionTokenRepository,
    private val emailRepository: EmailRepository
) {

    @Transactional(isolation = Isolation.DEFAULT) // TODO: VER TODOS OS CASOS DE ERRO DO COOKIE EM RELAÇAO AO TOKEN
    fun createUser(userInput: UserRegisterInput): AuthInformationDTO {

        emailRepository
            .findByEmail(userInput.email)
            .ifPresent {
                throw Exception("Email already exists") // TODO: criar exceção
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

        emailRepository.save(
            Email(
                email = userInput.email,
                user = user
            )
        )

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
     * Gets a user from the database identified by its [UserID] and returns it as a [UserOutput]
     * @param userID the user's id to be searched
     * @throws Exception if the user is not found     TODO: change commment
     */
    @Transactional(isolation = Isolation.DEFAULT)
    fun getUser(userID: UserID): UserOutput {
        val user = userRepository.findById(userID).orElseThrow { Exception("User not found") } // TODO: criar exceção
        return UserOutput(
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email?.email ?: throw Exception("never happens") // TODO: usar dto de user
        )
    }

    /**
     * Invalidates a session token, removing it from the database
     */
    @Transactional(isolation = Isolation.DEFAULT)
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
     * @throws Exception if the token is invalid //TODO: change comment
     */
    @Transactional(isolation = Isolation.DEFAULT)
    fun validateSessionToken(token: String): UserID {
        val sessionToken = sessionTokenRepository
            .findByToken(token) ?: throw Exception("Invalid token") // TODO: criar exceção

        if (sessionToken.hasExpired()) {
            sessionTokenRepository.delete(sessionToken)
            throw Exception("Token expired") // TODO: criar exceção e por la em cima no service para nao dar rollback
        }

        return sessionToken.user.id
    }

    /**
     * Confirms the user's login credentials
     * Reuses the session token if it is still valid otherwise creates a new one
     * @param userInput the user's login credentials
     * @returns a [AuthInformationDTO] object containing a session token and the user's id
     */
    fun authenticateUser(userInput: UserLoginInput): AuthInformationDTO {
        val email = emailRepository.findByEmail(userInput.email)
            .ifNotPresent {
                throw Exception("Email not found") // TODO: criar exceção
            }
        requireNotNull(email)

        val user = email.user

        if (hashPassword(userInput.password, user.passwordSalt) != user.passwordHash) {
            throw Exception("Invalid password") // TODO: criar exceção
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
                val timeToExpire = token.expiration.toInstant()
                    .minusMillis(SESSION_EXPIRATION_TIME)
                    .toMillis()

                return AuthInformationDTO(token.token, user.id, timeToExpire)
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
