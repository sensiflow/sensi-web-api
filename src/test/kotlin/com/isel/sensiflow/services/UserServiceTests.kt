package com.isel.sensiflow.services

import com.isel.sensiflow.Constants.User.SESSION_EXPIRATION_TIME
import com.isel.sensiflow.http.entities.input.UserLoginInput
import com.isel.sensiflow.http.entities.input.UserRegisterInput
import com.isel.sensiflow.http.entities.input.UserUpdateInput
import com.isel.sensiflow.model.entities.SessionToken
import com.isel.sensiflow.model.entities.User
import com.isel.sensiflow.model.entities.UserRole
import com.isel.sensiflow.model.repository.SessionTokenRepository
import com.isel.sensiflow.model.repository.UserRepository
import com.isel.sensiflow.model.repository.UserRoleRepository
import com.isel.sensiflow.services.dto.input.UserRoleInput
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
    private lateinit var tokenRepository: SessionTokenRepository

    @Mock
    private lateinit var userRoleRepository: UserRoleRepository



    @BeforeEach
    fun initMocks() {
        fakeUser.sessionTokens.add(fakeToken)

        MockitoAnnotations.openMocks(this)
    }

    private val ADMINRole = UserRole(
        id = 1,
        role = Role.ADMIN.name
    )

    private val userRole = UserRole(
        id = 2,
        role = Role.USER.name
    )

    private val MODRole = UserRole(
        id = 3,
        role = Role.MODERATOR.name
    )

    private val fakeUser = User(
        id = 1,
        firstName = "John",
        lastName = "Doe",
        role = ADMINRole,
        passwordHash = "cff70d1997acd2093cbfca9b66ace24a70deb47c4b5b4ec9f87b83f881432070a8708f2bcd578dc5466de2e07c88c314f76cab9719294e91bf99fb7f76770b9e",
        passwordSalt = "[B@55614340",
        email = "johnDoe@email.com"
    )

    private val fakeUser2 = User(
        id = 2,
        firstName = "Johnd",
        lastName = "Doed",
        role = MODRole,
        passwordHash = "cff70d1997acd2093cbfca9b66ace24a70deb47c4b5b4ec9f87b83f881432070a8708f2bcd578dc5466de2e07c88c314f76cab9719294e91bf99fb7f76770b9e",
        passwordSalt = "[B@55614340",
        email = "johnDoed@email.com"
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

        `when`(userRepository.findByEmail(fakeUser.email)).thenReturn(Optional.empty())

        `when`(userRoleRepository.findByRole(Role.USER.name)).thenReturn(Optional.of(userRole))
        `when`(userRepository.save(ArgumentMatchers.any(User::class.java))).thenReturn(fakeUser)
        `when`(tokenRepository.save(ArgumentMatchers.any(SessionToken::class.java))).thenReturn(fakeToken)

        val userID = userService.createUser(fakeUserInput)

        assertEquals(fakeUser.id, userID)

        verify(userRepository, times(1)).save(ArgumentMatchers.any(User::class.java))
    }

    @Test
    fun `trying to register a user with an existing email`() {
        `when`(userRepository.findByEmail(fakeUser.email)).thenReturn(Optional.of(fakeUser))
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
        assertEquals(fakeUser.email, result.email)
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
        `when`(userRepository.findByEmail(fakeUser.email)).thenReturn(Optional.of(fakeUser))
        `when`(tokenRepository.save(ArgumentMatchers.any(SessionToken::class.java))).thenReturn(fakeToken)

        val result = userService.authenticateUser(userLoginInput)

        assertEquals(fakeToken.token, result.token)
        assertEquals(fakeUser.id, result.userID)
    }

    @Test
    fun `login a user that already had a sessionToken`() {
        `when`(userRepository.findByEmail(fakeUser.email)).thenReturn(Optional.of(fakeUser))
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
        `when`(userRepository.findByEmail(fakeUser.email)).thenReturn(Optional.of(fakeUser))
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

        `when`(userRepository.findByEmail(fakeUser.email)).thenReturn(Optional.of(fakeUser))
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
        `when`(userRepository.findByEmail(fakeUser.email)).thenReturn(Optional.empty())
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

    @Test
    fun `delete a user sucessfully`() {
        `when`(userRepository.findById(fakeUser.id)).thenReturn(Optional.of(fakeUser))
        userService.deleteUser(fakeUser.id, 999999)
        verify(userRepository, times(1)).delete(fakeUser)
    }

    @Test
    fun `try to delete a user that does not exist`() {
        `when`(userRepository.findById(fakeUser.id)).thenReturn(Optional.empty())
        assertThrows<UserNotFoundException> {
            userService.deleteUser(fakeUser.id, 999999)
        }
    }

    @Test
    fun `try to delete the own user`() {
        `when`(userRepository.findById(fakeUser.id)).thenReturn(Optional.of(fakeUser))
        assertThrows<ActionForbiddenException> {
            userService.deleteUser(fakeUser.id, fakeUser.id)
        }
    }

    @Test
    fun `try to update a role with a non existant one`(){
        `when`(userRepository.findById(fakeUser.id)).thenReturn(Optional.of(fakeUser))
        assertThrows<RoleNotFoundException> {
            userService.updateRole(fakeUser.id, UserRoleInput("nonExistantRole"))
        }
    }

    @Test
    fun `update a role sucessfully while admin`(){
        `when`(userRoleRepository.findByRole("MODERATOR")).thenReturn(Optional.of(MODRole))
        `when`(userRepository.findById(fakeUser.id)).thenReturn(Optional.of(fakeUser))
        userService.updateRole(fakeUser.id, UserRoleInput("MODERATOR"))
        verify(userRepository, times(1)).save(ArgumentMatchers.any(User::class.java))
    }

    @Test
    fun  `Role wont update if the user has the same role as input`(){
        `when`(userRoleRepository.findByRole("ADMIN")).thenReturn(Optional.of(ADMINRole))
        `when`(userRepository.findById(fakeUser.id)).thenReturn(Optional.of(fakeUser))
        userService.updateRole(fakeUser.id, UserRoleInput("ADMIN"))
        verify(userRepository, times(0)).save(ArgumentMatchers.any(User::class.java))
    }

    @Test
    fun `try to update a role of a non existant user`(){
        `when`(userRoleRepository.findByRole("ADMIN")).thenReturn(Optional.of(ADMINRole))
        `when`(userRepository.findById(fakeUser.id)).thenReturn(Optional.empty())
        assertThrows<UserNotFoundException> {
            userService.updateRole(fakeUser.id, UserRoleInput("ADMIN"))
        }
    }


}
