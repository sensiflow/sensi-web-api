package com.isel.sensiflow.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.isel.sensiflow.http.entities.input.UserLoginInput
import com.isel.sensiflow.http.entities.input.UserRegisterInput
import com.isel.sensiflow.model.entities.Device
import com.isel.sensiflow.services.Role
import com.isel.sensiflow.services.beans.UserService
import com.isel.sensiflow.services.dto.output.PageDTO
import jakarta.servlet.http.Cookie
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

typealias NoBody = Unit

data class InvalidBody(
    val random: String = "",
    val timestamp: Long = 0,
    val value: Int = 0,
    val page: PageDTO<PageDTO<Device>>? = null
)

enum class HTTPMethod {
    GET, POST, PUT, DELETE
}

/**
 * Ensures that the cookie is not null
 * @param cookie The nullable cookie to be checked
 */
fun ensureCookieNotNull(cookie: Cookie?) = cookie ?: error("The provided cookie is null")

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
    role: Role,
    emailCount: Int,
): UserLoginInput {
    val user = UserRegisterInput(
        email = "test_$emailCount@email.com",
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
