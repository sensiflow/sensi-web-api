package com.isel.sensiflow.services

import com.isel.sensiflow.Constants.User.SESSION_EXPIRATION_TIME
import com.isel.sensiflow.http.entities.input.UserLoginInput
import com.isel.sensiflow.http.entities.input.UserRegisterInput
import com.isel.sensiflow.http.entities.input.UserUpdateInput
import com.isel.sensiflow.http.entities.input.fieldsAreEmpty
import com.isel.sensiflow.http.entities.input.isTheSameAS
import com.isel.sensiflow.http.entities.output.UserOutput
import com.isel.sensiflow.model.entities.SessionToken
import com.isel.sensiflow.model.entities.User
import com.isel.sensiflow.model.entities.hasExpired
import com.isel.sensiflow.model.entities.toDTO
import com.isel.sensiflow.model.entities.toRole
import com.isel.sensiflow.model.repository.SessionTokenRepository
import com.isel.sensiflow.model.repository.UserRepository
import com.isel.sensiflow.model.repository.UserRoleRepository
import com.isel.sensiflow.services.dto.AuthInformationDTO
import com.isel.sensiflow.services.dto.PageableDTO
import com.isel.sensiflow.services.dto.UserDTO
import com.isel.sensiflow.services.dto.input.UserRoleInput
import com.isel.sensiflow.services.dto.output.PageDTO
import com.isel.sensiflow.services.dto.output.toPageDTO
import com.isel.sensiflow.services.dto.toOutput
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val sessionTokenRepository: SessionTokenRepository,
    private val userRoleRepository: UserRoleRepository
) {

    /**
     * Creates a new user in the database and a session token for it
     * @param userInput the user's information
     * @return the user's id and session token
     * @throws EmailAlreadyExistsException if the email already exists
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun createUser(userInput: UserRegisterInput, role: Role = Role.USER): UserID { // TODO: add role to input
        userRepository
            .findByEmail(userInput.email)
            .ifPresent { throw EmailAlreadyExistsException(userInput.email) }

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
                passwordSalt = salt,
                email = userInput.email
            )
        )

        return user.id
    }

    /**
     * Gets a user from the database identified by its [UserID] and returns it as a [UserOutput]
     * @param userID the user's id to be searched
     * @throws UserNotFoundException if the [User] is not found
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    fun getUser(userID: UserID): UserDTO {
        val user = userRepository
            .findById(userID)
            .orElseThrow {
                UserNotFoundException(userID)
            }
        return user.toDTO()
    }

    /**
     * Gets all Users.
     * @param pageableDTO The pagination information used to get the users
     * @return a [PageDTO] containing the users
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    fun getUsers(pageableDTO: PageableDTO): PageDTO<UserOutput> {
        val pageable: Pageable = PageRequest.of(pageableDTO.page, pageableDTO.size)

        return userRepository
            .findAll(pageable)
            .map { it.toDTO().toOutput() }
            .toPageDTO()
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
    @Transactional(isolation = Isolation.REPEATABLE_READ, noRollbackFor = [InvalidTokenException::class])
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
        val user = userRepository.findByEmail(userInput.email)
            .orElseThrow { EmailNotFoundException(userInput.email) }

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
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun updateRole(userID: UserID, input: UserRoleInput) {
        val role = userRoleRepository.findByRole(input.role)
            .orElseThrow {
                RoleNotFoundException(input.role)
            }

        val user = userRepository.findById(userID)
            .orElseThrow { UserNotFoundException(userID) }

        if (user.role == role) {
            return
        }

        userRepository.save(
            User(
                id = user.id,
                firstName = user.firstName,
                lastName = user.lastName,
                role = role,
                passwordHash = user.passwordHash,
                passwordSalt = user.passwordSalt,
                email = user.email
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
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun updateUser(
        userID: UserID,
        invokerUserID: UserID,
        userInput: UserUpdateInput
    ) {
// TODO: documentaçao deste endpoint e do mudar a role, e dizer q um mod pode mudar coisas do user , mas nao d o admin
        val updaterUser = userRepository.findById(invokerUserID)
            .orElseThrow { UserNotFoundException(invokerUserID) }

        val user = userRepository.findById(userID)
            .orElseThrow { UserNotFoundException(userID) }

        // TODO: test this
        if (invokerUserID != userID && !updaterUser.role.toRole().isHigherThan(user.role.toRole()) && updaterUser.role.toRole() != Role.ADMIN) {
            throw ActionForbiddenException("You don't have permission to update this user")
        }

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
            role = user.role,
            email = user.email
        )

        userRepository.save(updatedUser)
    }

    /**
     * Deletes a user from the database
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun deleteUser(id: UserID, updatedUserID: UserID) {
        if (id == updatedUserID) {
            throw ActionForbiddenException("You can't delete yourself")
        }
        val user = userRepository.findById(id)
            .orElseThrow { UserNotFoundException(id) }

        userRepository.delete(user)
    }
}
