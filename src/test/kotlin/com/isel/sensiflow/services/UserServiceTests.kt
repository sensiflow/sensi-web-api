package com.isel.sensiflow.services

import com.isel.sensiflow.Constants.User.SESSION_EXPIRATION_TIME
import com.isel.sensiflow.http.entities.input.UserLoginInput
import com.isel.sensiflow.http.entities.input.UserRegisterInput
import com.isel.sensiflow.http.entities.input.UserUpdateInput
import com.isel.sensiflow.model.dao.Email
import com.isel.sensiflow.model.dao.SessionToken
import com.isel.sensiflow.model.dao.User
import com.isel.sensiflow.model.dao.Userrole
import com.isel.sensiflow.model.repository.EmailRepository
import com.isel.sensiflow.model.repository.SessionTokenRepository
import com.isel.sensiflow.model.repository.UserRepository
import com.isel.sensiflow.model.repository.UserRoleRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.util.Optional

@RunWith(MockitoJUnitRunner::class)
class UserServiceTests {

    @InjectMocks
    private lateinit var userService: UserService

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var emailRepository: EmailRepository

    @Mock
    private lateinit var tokenRepository: SessionTokenRepository

    @Mock
    private lateinit var userRoleRepository: UserRoleRepository

    @BeforeEach
    fun initMocks() {
        fakeUser.email = fakeUserEmail
        fakeUser.sessionTokens.add(fakeToken)

        MockitoAnnotations.openMocks(this)
    }

    private val ADMINRole = Userrole(
        id = 1,
        role = Role.ADMIN.name
    )

    private val userRole = Userrole(
        id = 2,
        role = Role.USER.name
    )

    private val fakeUser = User(
        id = 1,
        firstName = "John",
        lastName = "Doe",
        role = ADMINRole,
        passwordHash = "cff70d1997acd2093cbfca9b66ace24a70deb47c4b5b4ec9f87b83f881432070a8708f2bcd578dc5466de2e07c88c314f76cab9719294e91bf99fb7f76770b9e",
        passwordSalt = "[B@55614340"
    )

    private val fakeUserEmail = Email(
        user = fakeUser,
        email = "johnDoe@email.com"
    )

    private val timeNow = System.currentTimeMillis()
    private val fakeToken = SessionToken(
        token = "token",
        user = fakeUser,
        expiration = (timeNow + SESSION_EXPIRATION_TIME).toTimeStamp()
    )

    private val fakeUserInput = UserRegisterInput(
        firstName = "John",
        lastName = "Doe",
        email = "johnDoe@email.com",
        password = "Passord2.0"
    )

    private val userLoginInput = UserLoginInput(
        email = "johnDoe@email.com",
        password = "Passord2.0"
    )

    @Test
    fun `register user successfully`() {

        `when`(emailRepository.findByEmail(fakeUserEmail.email)).thenReturn(null)

        `when`(userRoleRepository.findByRole(Role.USER.name)).thenReturn(Optional.of(userRole))
        `when`(emailRepository.save(ArgumentMatchers.any(Email::class.java))).thenReturn(fakeUserEmail)
        `when`(userRepository.save(ArgumentMatchers.any(User::class.java))).thenReturn(fakeUser)
        `when`(tokenRepository.save(ArgumentMatchers.any(SessionToken::class.java))).thenReturn(fakeToken)

        val userID = userService.createUser(fakeUserInput)

        assertEquals(fakeUser.id, userID)

        verify(emailRepository, times(1)).findByEmail(fakeUserEmail.email)
        verify(userRepository, times(2)).save(ArgumentMatchers.any(User::class.java))
    }

    @Test
    fun `trying to register a user with an existing email`() {
        `when`(emailRepository.findByEmail(fakeUserEmail.email)).thenReturn(fakeUserEmail)
        assertThrows<EmailAlreadyExistsException> {
            userService.createUser(fakeUserInput)
        }
        verify(userRepository, times(0)).save(ArgumentMatchers.any(User::class.java))
    }

    @Test
    fun `get an existing user`() {
        `when`(userRepository.findById(fakeUser.id)).thenReturn(Optional.of(fakeUser))
        val result = userService.getUser(fakeUser.id)
        assertEquals(fakeUser.firstName, result.firstName)
        assertEquals(fakeUser.lastName, result.lastName)
        assertEquals(fakeUser.email.email, result.email)
        assertTrue(fakeUser.sessionTokens.contains(fakeToken))
    }

    @Test
    fun `Try to get a non existing user`() {
        `when`(userRepository.findById(fakeUser.id)).thenReturn(Optional.empty())
        assertThrows<UserNotFoundException> {
            userService.getUser(fakeUser.id)
        }
    }

    @Test
    fun `login a user successfully`() {
        `when`(emailRepository.findByEmail(fakeUserEmail.email)).thenReturn(fakeUserEmail)
        `when`(tokenRepository.save(ArgumentMatchers.any(SessionToken::class.java))).thenReturn(fakeToken)

        val result = userService.authenticateUser(userLoginInput)

        assertEquals(fakeToken.token, result.token)
        assertEquals(fakeUser.id, result.userID)
    }

    @Test
    fun `login a user that already had a sessionToken`() {
        `when`(emailRepository.findByEmail(fakeUserEmail.email)).thenReturn(fakeUserEmail)
        `when`(tokenRepository.findByUser(fakeUser)).thenReturn(fakeToken)
        `when`(tokenRepository.save(ArgumentMatchers.any(SessionToken::class.java))).thenReturn(fakeToken)

        val result = userService.authenticateUser(userLoginInput)

        assertEquals(fakeToken.token, result.token)
        assertEquals(fakeUser.id, result.userID)
        assertTrue(result.timeUntilExpire > 0) // Future time

        verify(tokenRepository, times(1)).delete(fakeToken)
        verify(tokenRepository, times(1)).save(ArgumentMatchers.any())
    }

    @Test
    fun `login a user with a wrong password`() {
        `when`(emailRepository.findByEmail(fakeUserEmail.email)).thenReturn(fakeUserEmail)
        assertThrows<InvalidCredentialsException> {
            userService.authenticateUser(userLoginInput.copy(password = "wrongPassword"))
        }
    }

    @Test
    fun `login a user with an expired token`() {
        val fakeExpiredToken = SessionToken(
            token = "token",
            user = fakeUser,
            expiration = (timeNow - SESSION_EXPIRATION_TIME).toTimeStamp()
        )

        `when`(emailRepository.findByEmail(fakeUserEmail.email)).thenReturn(fakeUserEmail)
        `when`(tokenRepository.findByUser(fakeUser)).thenReturn(fakeExpiredToken)
        `when`(tokenRepository.save(ArgumentMatchers.any(SessionToken::class.java))).thenReturn(fakeToken)

        val result = userService.authenticateUser(userLoginInput)

        assertEquals(fakeToken.token, result.token)
        assertEquals(fakeUser.id, result.userID)

        verify(tokenRepository, times(1)).delete(fakeExpiredToken)
        verify(tokenRepository, times(1)).save(ArgumentMatchers.any())
    }

    @Test
    fun `try to login a user with an invalid email`() {
        `when`(emailRepository.findByEmail(fakeUserEmail.email)).thenReturn(null)
        assertThrows<EmailNotFoundException> {
            userService.authenticateUser(userLoginInput)
        }
    }

    @Test
    fun `logout successfully`() {
        `when`(tokenRepository.findByToken(fakeToken.token)).thenReturn(fakeToken)
        userService.invalidateSessionToken(fakeToken.token)
        verify(tokenRepository, times(1)).delete(fakeToken)
    }

    @Test
    fun `validate successfully a given valid token`() {
        `when`(tokenRepository.findByToken(fakeToken.token)).thenReturn(fakeToken)
        val result = userService.validateSessionToken(fakeToken.token)
        assertEquals(fakeUser.id, result)
    }

    @Test
    fun `validate a given invalid token`() {
        `when`(tokenRepository.findByToken(fakeToken.token)).thenReturn(null)
        assertThrows<InvalidTokenException> {
            userService.validateSessionToken(fakeToken.token)
        }
    }

    @Test
    fun `validate a given expired token`() {
        val expiredToken = SessionToken(
            token = "token",
            user = fakeUser,
            expiration = (System.currentTimeMillis() - SESSION_EXPIRATION_TIME).toTimeStamp()
        )

        `when`(tokenRepository.findByToken(fakeToken.token)).thenReturn(expiredToken)
        assertThrows<InvalidTokenException> {
            userService.validateSessionToken(fakeToken.token)
        }
    }

    @Test
    fun `change a users info`() {
        `when`(userRepository.findById(fakeUser.id)).thenReturn(Optional.of(fakeUser))
        val userInput = UserUpdateInput(
            password = "newPassword1.",
            firstName = "newFirstName",
            lastName = "newLastName"
        )
        userService.updateUser(fakeUser.id, fakeUser.id, userInput)
        verify(userRepository, times(1)).save(ArgumentMatchers.any(User::class.java))
    }

    @Test
    fun `change a users info with the old info`() {
        `when`(userRepository.findById(fakeUser.id)).thenReturn(Optional.of(fakeUser))
        val userInput = UserUpdateInput(
            password = "Passord2.0",
            firstName = fakeUser.firstName,
            lastName = fakeUser.lastName
        )
        userService.updateUser(fakeUser.id, fakeUser.id, userInput)
        verify(userRepository, times(0)).save(ArgumentMatchers.any(User::class.java))
    }

    @Test
    fun `try to update a user without sending any fields`() {
        `when`(userRepository.findById(fakeUser.id)).thenReturn(Optional.of(fakeUser))
        val userInput = UserUpdateInput()

        userService.updateUser(fakeUser.id, fakeUser.id, userInput)

        verify(userRepository, times(0)).save(ArgumentMatchers.any(User::class.java))
    }
}
