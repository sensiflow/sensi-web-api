package com.isel.sensiflow.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.isel.sensiflow.Constants
import com.isel.sensiflow.Constants.Problem.URI.URI_VALIDATION_ERROR
import com.isel.sensiflow.http.entities.input.UserLoginInput
import com.isel.sensiflow.http.entities.input.UserRegisterInput
import com.isel.sensiflow.http.entities.output.IDOutput
import com.isel.sensiflow.services.UserID
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(org.springframework.test.context.junit4.SpringRunner::class)
@AutoConfigureMockMvc
@Transactional
class UserControllerTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    companion object {
        val mapper = jacksonObjectMapper()
    }

    private fun createUser(
        user: UserRegisterInput = UserRegisterInput(
            email = "test@email.com",
            firstName = "Test",
            lastName = "Test",
            password = "Password1_"
        )
    ): Pair<UserID, Cookie?> {
        val json = DevicesGroupControllerTests.mapper.writeValueAsString(user)

        val result = mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isCreated)
            .andReturn()

        val response = result.response.contentAsString
        val id = mapper.readValue(response, IDOutput::class.java)

        return Pair(id.id, result.response.getCookie(Constants.User.AUTH_COOKIE_NAME))
    }

    @Test
    fun `test user register`() {
        val userRegisterInput = UserRegisterInput(
            email = "test@email.com",
            firstName = "John",
            lastName = "Pork",
            password = "JosePuerco123."
        )
        val json = mapper.writeValueAsString(userRegisterInput)

        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").isNumber)
    }

    @Test
    fun `test user register with invalid email`() {
        val userRegisterInput = UserRegisterInput(
            email = "testemail.com",
            firstName = "John",
            lastName = "Pork",
            password = "JosePuerco123."
        )
        val json = mapper.writeValueAsString(userRegisterInput)

        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value(URI_VALIDATION_ERROR))
    }

    @Test
    fun `test user register with a small password`() {
        val userRegisterInput = UserRegisterInput(
            email = "teste@email.com",
            firstName = "John",
            lastName = "Pork",
            password = ""
        )
        val json = mapper.writeValueAsString(userRegisterInput)

        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value(URI_VALIDATION_ERROR))
    }

    @Test
    fun `test user register with invalid first name`() {
        val userRegisterInput = UserRegisterInput(
            email = "test@email.com",
            firstName = "",
            lastName = "Pork",
            password = "JosePuerco123."
        )
        val json = mapper.writeValueAsString(userRegisterInput)

        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value(URI_VALIDATION_ERROR))
    }

    @Test
    fun `test user register with invalid last name`() {
        val userRegisterInput = UserRegisterInput(
            email = "test@email.com",
            firstName = "John",
            lastName = "",
            password = "JosePuerco123."
        )
        val json = mapper.writeValueAsString(userRegisterInput)

        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value(URI_VALIDATION_ERROR))
    }

    @Test
    fun `test user register with invalid password`() {
        val userRegisterInput = UserRegisterInput(
            email = "test@email.com",
            firstName = "John",
            lastName = "Pork",
            password = "Joao"
        )
        val json = mapper.writeValueAsString(userRegisterInput)

        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value(URI_VALIDATION_ERROR))
            .andReturn().response.contentAsString
    }

    @Test
    fun `test user register with blank first name last name and password`() {
        val userRegisterInput = UserRegisterInput(
            email = "testemail.com",
            firstName = "",
            lastName = "",
            password = ""
        )
        val json = mapper.writeValueAsString(userRegisterInput)

        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value(URI_VALIDATION_ERROR))
    }

    @Test
    fun `test user register with null parameters`() {

        val json = """{"email":"testemail.com","firstName":"sdfwe"}"""

        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value(Constants.Problem.URI.INVALID_JSON_BODY))
    }

    @Test
    fun `login sucessfully`() {
        val (id, cookie) = createUser(
            UserRegisterInput(email = "test@email.com", firstName = "Test", lastName = "Test", password = "Password1_.")
        )
        requireNotNull(cookie)

        val userLogin = UserLoginInput(
            email = "test@email.com",
            password = "Password1_."
        )
        val json = mapper.writeValueAsString(userLogin)

        mockMvc.perform(
            post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(cookie().value(Constants.User.AUTH_COOKIE_NAME, cookie.value))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(id))
    }

    @Test
    fun `try to login with invalid credentials`() {
        val (id, cookie) = createUser(
            UserRegisterInput(email = "test@email.com", firstName = "Test", lastName = "Test", password = "Password1_")
        )

        val userLogin = UserLoginInput(
            email = "test@email.com",
            password = "JosePue123."
        )
        val json = mapper.writeValueAsString(userLogin)

        mockMvc.perform(
            post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isUnauthorized)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value(Constants.Problem.URI.INVALID_CREDENTIALS))
    }

    @Test
    fun `try to login without a createdUser`() {
        val userLogin = UserLoginInput(
            email = "test@email.com",
            password = "JosePue123."
        )
        val json = mapper.writeValueAsString(userLogin)

        mockMvc.perform(
            post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value(Constants.Problem.URI.EMAIL_NOT_FOUND))
    }

    @Test
    fun `try to login with an invalid password field`() {
        val json = """{"email":"test@email.com"}"""

        mockMvc.perform(
            post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value(Constants.Problem.URI.INVALID_JSON_BODY))
    }

    @Test
    fun `get a user sucessfully`() {
        val (id, cookie) = createUser(
            UserRegisterInput(email = "test@email.com", firstName = "Test", lastName = "Test", password = "Password1_")
        )

        mockMvc.perform(
            MockMvcRequestBuilders.get("/users/$id")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.email").value("test@email.com"))
            .andExpect(jsonPath("$.firstName").value("Test"))
            .andExpect(jsonPath("$.lastName").value("Test"))
    }

    @Test
    fun `get a user with non existent id`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/users/1000")
        )
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value(Constants.Problem.URI.USER_NOT_FOUND))
    }

    @Test
    fun `get a user with invalid id`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/users/invalidId")
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value(Constants.Problem.URI.URI_VALIDATION_ERROR))
    }

    @Test
    fun `logout sucessfully`() {
        val (id, cookie) = createUser(
            UserRegisterInput(email = "test@email.com", firstName = "Test", lastName = "Test", password = "Password1_")
        )
        requireNotNull(cookie)

        mockMvc.perform(
            post("/users/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cookie.value)
                .cookie(cookie)
        )
            .andExpect(status().isNoContent)
            .andExpect(cookie().maxAge(Constants.User.AUTH_COOKIE_NAME, 0))
    }

    @Test
    fun `try to logout without a cookie`() {
        mockMvc.perform(
            post("/users/logout")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isUnauthorized)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value(Constants.Problem.URI.UNAUTHENTICATED))
    }
}
