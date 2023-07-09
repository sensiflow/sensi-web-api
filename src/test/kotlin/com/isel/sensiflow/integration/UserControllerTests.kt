package com.isel.sensiflow.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.isel.sensiflow.Constants
import com.isel.sensiflow.Constants.Problem.URI.URI_VALIDATION_ERROR
import com.isel.sensiflow.http.entities.input.UserLoginInput
import com.isel.sensiflow.http.entities.input.UserRegisterInput
import com.isel.sensiflow.http.entities.input.UserUpdateInput
import com.isel.sensiflow.http.entities.output.AuthOutput
import com.isel.sensiflow.http.entities.output.IDOutput
import com.isel.sensiflow.http.entities.output.UserOutput
import com.isel.sensiflow.integration.HTTPMethod.GET
import com.isel.sensiflow.integration.HTTPMethod.POST
import com.isel.sensiflow.services.ID
import com.isel.sensiflow.services.Role.ADMIN
import com.isel.sensiflow.services.beans.UserService
import com.isel.sensiflow.services.dto.input.UserRoleInput
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(org.springframework.test.context.junit4.SpringRunner::class)
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class UserControllerTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userService: UserService

    companion object {
        private val mapper = jacksonObjectMapper()
        private var counter = 0
    }

    @Test
    fun `test user register`() {
        val cookie = ensureCookieNotNull(cookie = getCookie())

        val userRegisterInput = UserRegisterInput(
            email = "test@email.com",
            firstName = "John",
            lastName = "Pork",
            password = "JosePuerco123."
        )

        mockMvc.request<UserRegisterInput, IDOutput>(
            method = POST,
            uri = "/users",
            body = userRegisterInput,
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isCreated)
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").isNumber)
            }

        )
    }

    @Test
    fun `test user register with invalid email`() {
        val cookie = ensureCookieNotNull(cookie = getCookie())

        val userRegisterInput = UserRegisterInput(
            email = "testemail.com",
            firstName = "John",
            lastName = "Pork",
            password = "JosePuerco123."
        )

        mockMvc.request<UserRegisterInput, ProblemDetail>(
            method = POST,
            uri = "/users",
            body = userRegisterInput,
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isBadRequest)
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value(URI_VALIDATION_ERROR))
            }
        )
    }

    @Test
    fun `test user register with a small password`() {
        val cookie = ensureCookieNotNull(cookie = getCookie())

        val userRegisterInput = UserRegisterInput(
            email = "teste@email.com",
            firstName = "John",
            lastName = "Pork",
            password = ""
        )

        mockMvc.request<UserRegisterInput, ProblemDetail>(
            method = POST,
            uri = "/users",
            body = userRegisterInput,
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isBadRequest)
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value(URI_VALIDATION_ERROR))
            }
        )
    }

    @Test
    fun `test user register with invalid first name`() {
        val cookie = ensureCookieNotNull(cookie = getCookie())

        val userRegisterInput = UserRegisterInput(
            email = "test@email.com",
            firstName = "",
            lastName = "Pork",
            password = "JosePuerco123."
        )

        mockMvc.request<UserRegisterInput, ProblemDetail>(
            method = POST,
            uri = "/users",
            body = userRegisterInput,
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isBadRequest)
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value(URI_VALIDATION_ERROR))
            }
        )
    }

    @Test
    fun `test user register with invalid last name`() {
        val cookie = ensureCookieNotNull(cookie = getCookie())

        val userRegisterInput = UserRegisterInput(
            email = "test@email.com",
            firstName = "John",
            lastName = "",
            password = "JosePuerco123."
        )

        mockMvc.request<UserRegisterInput, ProblemDetail>(
            method = POST,
            uri = "/users",
            body = userRegisterInput,
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isBadRequest)
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value(URI_VALIDATION_ERROR))
            }
        )
    }

    @Test
    fun `test user register with invalid password`() {
        val cookie = ensureCookieNotNull(cookie = getCookie())

        val userRegisterInput = UserRegisterInput(
            email = "test@email.com",
            firstName = "John",
            lastName = "Pork",
            password = "Joao"
        )

        mockMvc.request<UserRegisterInput, ProblemDetail>(
            method = POST,
            uri = "/users",
            body = userRegisterInput,
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isBadRequest)
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value(URI_VALIDATION_ERROR))
            }
        )
    }

    @Test
    fun `test user register with blank first name last name and password`() {
        val cookie = ensureCookieNotNull(cookie = getCookie())

        val userRegisterInput = UserRegisterInput(
            email = "testemail.com",
            firstName = "",
            lastName = "",
            password = ""
        )

        mockMvc.request<UserRegisterInput, ProblemDetail>(
            method = POST,
            uri = "/users",
            body = userRegisterInput,
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isBadRequest)
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value(URI_VALIDATION_ERROR))
            }
        )
    }

    data class InvalidRegisterInput(val email: String, val firstName: String)

    @Test
    fun `test user register with null parameters`() {
        val cookie = ensureCookieNotNull(cookie = getCookie())
        val body = InvalidRegisterInput(email = "testemail.com", firstName = "sdfwe")

        mockMvc.request<InvalidRegisterInput, ProblemDetail>(
            method = POST,
            uri = "/users",
            body = body,
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isBadRequest)
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value(Constants.Problem.URI.INVALID_JSON_BODY))
            }
        )
    }

    @Test
    fun `login successfully`() {
        val cookie = ensureCookieNotNull(cookie = getCookie())

        val body = UserRegisterInput(email = "test@email.com", firstName = "Test", lastName = "Test", password = "Password1_.")
        val createUserResponse = mockMvc.request<UserRegisterInput, IDOutput>(
            method = POST,
            uri = "/users",
            body = body,
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isCreated)
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").isNotEmpty)
            }
        )

        val userLogin = UserLoginInput(
            email = "test@email.com",
            password = "Password1_."
        )

        mockMvc.request<UserLoginInput, AuthOutput>(
            method = POST,
            uri = "/users/login",
            body = userLogin,
            mapper = mapper,
            assertions = {
                andExpect(status().isOk)
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").isNotEmpty)
                    .andExpect(jsonPath("$.id").value(createUserResponse?.id))
            }
        )
    }

    @Test
    fun `try to login with invalid credentials`() {
        val cookie = ensureCookieNotNull(cookie = getCookie())

        val body = UserRegisterInput(email = "test@email.com", firstName = "Test", lastName = "Test", password = "Password1_.")
        mockMvc.request<UserRegisterInput, AuthOutput>(
            method = POST,
            uri = "/users",
            body = body,
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isCreated)
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").isNotEmpty)
            }
        )

        val userLogin = UserLoginInput(
            email = "test@email.com",
            password = "JosePue123."
        )

        mockMvc.request<UserLoginInput, ProblemDetail>(
            method = POST,
            uri = "/users/login",
            body = userLogin,
            mapper = mapper,
            assertions = {
                andExpect(status().isUnauthorized)
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value(Constants.Problem.URI.INVALID_CREDENTIALS))
            }
        )
    }

    @Test
    fun `try to login without a createdUser`() {
        val userLogin = UserLoginInput(
            email = "test@email.com",
            password = "JosePue123."
        )

        mockMvc.request<UserLoginInput, ProblemDetail>(
            method = POST,
            uri = "/users/login",
            body = userLogin,
            mapper = mapper,
            assertions = {
                andExpect(status().isNotFound)
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value(Constants.Problem.URI.EMAIL_NOT_FOUND))
            }
        )
    }

    data class InvalidLoginInput(val email: String, val password: String? = null)

    @Test
    fun `try to login with an invalid password field`() {
        val body = InvalidLoginInput(email = "test@email.com")

        mockMvc.request<InvalidLoginInput, ProblemDetail>(
            method = POST,
            uri = "/users/login",
            body = body,
            mapper = mapper,
            assertions = {
                andExpect(status().isBadRequest)
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value(Constants.Problem.URI.INVALID_JSON_BODY))
            }
        )
    }

    @Test
    fun `get a user sucessfully`() {
        val cookie = ensureCookieNotNull(cookie = getCookie())

        val body = UserRegisterInput(email = "test@email.com", firstName = "Test", lastName = "Test", password = "Password1_.")
        val userCreationResponse = mockMvc.request<UserRegisterInput, IDOutput>(
            method = POST,
            uri = "/users",
            body = body,
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isCreated)
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").isNotEmpty)
            }
        )

        mockMvc.request<Unit, UserOutput>(
            method = GET,
            uri = "/users/${userCreationResponse?.id}",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isOk)
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.email").value("test@email.com"))
                    .andExpect(jsonPath("$.firstName").value("Test"))
                    .andExpect(jsonPath("$.lastName").value("Test"))
            }
        )
    }

    @Test
    fun `get a user with non existent id`() {
        mockMvc.request<Unit, ProblemDetail>(
            method = GET,
            uri = "/users/1000",
            mapper = mapper,
            assertions = {
                andExpect(status().isNotFound)
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value(Constants.Problem.URI.USER_NOT_FOUND))
            }
        )
    }

    @Test
    fun `get a user with invalid id`() {
        mockMvc.request<Unit, ProblemDetail>(
            method = GET,
            uri = "/users/invalidId",
            mapper = mapper,
            assertions = {
                andExpect(status().isBadRequest)
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value(URI_VALIDATION_ERROR))
            }
        )
    }

    @Test
    fun `delete a user sucessfully`() {
        val cookie = ensureCookieNotNull(cookie = getCookie())

        val body = UserRegisterInput(email = "test@email.com", firstName = "Test", lastName = "Test", password = "Password1_.")

        val userCreationResponse = mockMvc.request<UserRegisterInput, IDOutput>(
            method = POST,
            uri = "/users",
            body = body,
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isCreated)
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").isNotEmpty)
            }
        )

        mockMvc.request<UserLoginInput, IDOutput>(
            method = HTTPMethod.DELETE,
            uri = "/users/${userCreationResponse?.id}",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNoContent)
            }
        )
    }

    @Test
    fun `try to delete a user with non existent id`() {
        val cookie = ensureCookieNotNull(cookie = getCookie())
        mockMvc.request<UserLoginInput, ProblemDetail>(
            method = HTTPMethod.DELETE,
            uri = "/users/1000",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNotFound)
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value(Constants.Problem.URI.USER_NOT_FOUND))
            }
        )
    }

    @Test
    fun `try to delete the own user`() {
        val testUserInfo = createAdminTestUser()

        mockMvc.request<UserLoginInput, ProblemDetail>(
            method = HTTPMethod.DELETE,
            uri = "/users/${testUserInfo.id}",
            authorization = testUserInfo.cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isForbidden)
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value(Constants.Problem.URI.FORBIDDEN))
            }
        )
    }

    @Test
    fun `logout successfully`() {
        val cookie = ensureCookieNotNull(cookie = getCookie())

        val body = UserRegisterInput(email = "test@email.com", firstName = "Test", lastName = "Test", password = "Password1_.")
        mockMvc.request<UserRegisterInput, IDOutput>(
            method = POST,
            uri = "/users",
            body = body,
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isCreated)
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").isNotEmpty)
            }
        )

        mockMvc.request<UserRegisterInput, IDOutput>(
            method = POST,
            uri = "/users/logout",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNoContent)
                    .andExpect(cookie().maxAge(Constants.User.AUTH_COOKIE_NAME, 0))
            }
        )
    }

    @Test
    fun `try to logout without a cookie`() {

        mockMvc.request<UserRegisterInput, ProblemDetail>(
            method = POST,
            uri = "/users/logout",
            mapper = mapper,
            assertions = {
                andExpect(status().isUnauthorized)
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value(Constants.Problem.URI.UNAUTHENTICATED))
            }
        )
    }

    @Test
    fun `update a user sucessfully`() {
        val (id, cookie, loginInput) = createAdminTestUser()

        val updateBody = UserUpdateInput(
            firstName = "Test1",
            lastName = "Test1",
            password = "Password1_.2"
        )

        mockMvc.request<UserLoginInput, UserOutput>(
            method = GET,
            uri = "/users/$id",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isOk)
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.firstName").value("Test"))
                    .andExpect(jsonPath("$.lastName").value("Test"))
                    .andExpect(jsonPath("$.role").value("ADMIN"))
            }
        )

        mockMvc.request<UserUpdateInput, ProblemDetail>(
            method = HTTPMethod.PUT,
            uri = "/users/$id",
            body = updateBody,
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNoContent)
            }
        )

        // Can log in with the new password
        mockMvc.request<UserLoginInput, AuthOutput>(
            method = POST,
            uri = "/users/login",
            authorization = cookie,
            body = UserLoginInput(email = loginInput.email, password = updateBody.password!!),
            mapper = mapper,
            assertions = {
                andExpect(status().isOk)
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.expiresIn").isNotEmpty)
            }
        )

        mockMvc.request<NoBody, UserOutput>(
            method = GET,
            uri = "/users/$id",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isOk)
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.firstName").value(updateBody.firstName))
                    .andExpect(jsonPath("$.lastName").value(updateBody.lastName))
                    .andExpect(jsonPath("$.role").value("ADMIN"))
            }
        )
    }

    @Test
    fun `try to update a user's role to a non existent role`() {
        val (id, cookie, _) = createAdminTestUser()

        val updateBody = UserRoleInput(
            "NON_EXISTROLE"
        )

        mockMvc.request<UserRoleInput, ProblemDetail>(
            method = HTTPMethod.PUT,
            uri = "/users/$id/role",
            body = updateBody,
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNotFound)
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value(Constants.Problem.URI.ROLE_NOT_FOUND))
            }
        )
    }

    @Test
    fun `try to update the role of a non existant user`() {
        val (_, cookie, _) = createAdminTestUser()

        val updateBody = UserRoleInput(
            "ADMIN"
        )

        mockMvc.request<UserRoleInput, ProblemDetail>(
            method = HTTPMethod.PUT,
            uri = "/users/765434565/role",
            body = updateBody,
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNotFound)
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value(Constants.Problem.URI.USER_NOT_FOUND))
            }
        )
    }

    @Test
    fun `update a role sucessfully`() {
        val (id, cookie, _) = createAdminTestUser()

        val updateBody = UserRoleInput(
            "MODERATOR"
        )

        mockMvc.request<UserRoleInput, Unit>(
            method = HTTPMethod.PUT,
            uri = "/users/$id/role",
            body = updateBody,
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNoContent)
            }
        )

        mockMvc.request<Unit, UserOutput>(
            method = GET,
            uri = "/users/$id",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isOk)
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.role").value("MODERATOR"))
            }
        )
    }

    private fun createAdminTestUser(): TestUserInfo {
        val loginInput = createTestUser(userService, role = ADMIN, counter++)
        val loginJson = mapper.writeValueAsString(loginInput)

        val loginResult = mockMvc.perform(
            post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson)
        ).andExpect(status().isOk)

        val id = mapper.readValue(
            loginResult
                .andReturn()
                .response
                .contentAsString,
            AuthOutput::class.java
        ).id

        val cookie = loginResult
            .andReturn()
            .response
            .getCookie(Constants.User.AUTH_COOKIE_NAME)
        require(cookie != null)

        return TestUserInfo(id, cookie, loginInput)
    }

    private data class TestUserInfo(
        val id: ID,
        val cookie: Cookie,
        val loginInput: UserLoginInput
    )

    private fun getCookie(emailCounter: Int = counter++): Cookie? {
        val loginInput = createTestUser(userService, role = ADMIN, emailCounter)
        val loginJson = mapper.writeValueAsString(loginInput)

        val loginResult = mockMvc.perform(
            post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson)
        ).andExpect(status().isOk)

        return loginResult
            .andReturn()
            .response
            .getCookie(Constants.User.AUTH_COOKIE_NAME)
    }
}
