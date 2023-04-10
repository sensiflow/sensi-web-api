package com.isel.sensiflow.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.isel.sensiflow.http.entities.input.UserLoginInput
import com.isel.sensiflow.http.entities.input.UserRegisterInput
import com.isel.sensiflow.services.Role
import com.isel.sensiflow.services.UserService
import jakarta.servlet.http.Cookie
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

enum class HTTPMethod {
    GET, POST, PUT, DELETE
}

fun ensureCookieNotNull(cookie: Cookie?) = cookie ?: error("Failed to create user")

inline fun <reified T, reified R> MockMvc.request(
    method: HTTPMethod,
    uri: String,
    body: T? = null,
    authorization: Cookie? = null,
    mapper: ObjectMapper,
    assertions: ResultActions.() -> ResultActions = { this }
): R? {
    val requestBuilder = when (method) {
        HTTPMethod.GET -> MockMvcRequestBuilders.get(uri)
        HTTPMethod.POST -> MockMvcRequestBuilders.post(uri)
        HTTPMethod.PUT -> MockMvcRequestBuilders.put(uri)
        HTTPMethod.DELETE -> MockMvcRequestBuilders.delete(uri)
    }

    val response = perform(
        requestBuilder
            .contentType(MediaType.APPLICATION_JSON)
            .addIfExists(body, mapper)
            .addIfExists(authorization)
    )
        .assertions()
        .andReturn().response.contentAsString

    if (response.isEmpty()) return null
    return mapper.readValue(response, R::class.java)
}

inline fun <reified T> MockHttpServletRequestBuilder.addIfExists(
    body: T?,
    mapper: ObjectMapper
): MockHttpServletRequestBuilder {
    body ?: return this
    val json = mapper.writeValueAsString(body)
    return this.content(json)
}

fun MockHttpServletRequestBuilder.addIfExists(authorization: Cookie?): MockHttpServletRequestBuilder =
    if (authorization != null) this.cookie(authorization) else this

fun createTestUser(
    userService: UserService,
    role: Role
): UserLoginInput {
    val user = UserRegisterInput(
        email = "owner_test@email.com",
        firstName = "Test",
        lastName = "Test",
        password = "Password1_"
    )

    userService.createUser(user, role)
    return UserLoginInput(
        email = user.email,
        password = user.password
    )
}
