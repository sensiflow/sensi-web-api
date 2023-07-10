package com.isel.sensiflow.authorization

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.isel.sensiflow.Constants
import com.isel.sensiflow.http.entities.output.IDOutput
import com.isel.sensiflow.integration.HTTPMethod
import com.isel.sensiflow.integration.createTestUser
import com.isel.sensiflow.integration.ensureCookieNotNull
import com.isel.sensiflow.integration.request
import com.isel.sensiflow.services.ID
import com.isel.sensiflow.services.Role
import com.isel.sensiflow.services.beans.UserService
import com.isel.sensiflow.services.dto.input.DeviceInputDTO
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

/**
 * Tests related with the authorization hierarchy.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(org.springframework.test.context.junit4.SpringRunner::class)
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class AuthorizationTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userService: UserService

    companion object {
        private val mapper = jacksonObjectMapper()
        private var counter = 0
        private const val VALID_STREAM_URL = "rtsp://sensiflow.zapto.org:554/1/stream1"
    }

    @Test
    fun `Roles higher in the hierarchy have access to endpoints of lower roles`() {
        val moderatorCookie = ensureCookieNotNull(cookie = getCookie(Role.MODERATOR))
        val ADMINCookie = ensureCookieNotNull(cookie = getCookie(Role.ADMIN))

        val input = DeviceInputDTO(
            name = "Test",
            description = "Test",
            streamURL = VALID_STREAM_URL
        )

        createDevice(moderatorCookie, input)
        createDevice(ADMINCookie, input)
    }

    @Test
    fun `Roles below in the hierarchy do not have access to endpoints of higher roles`() {
        val moderatorCookie = ensureCookieNotNull(cookie = getCookie(Role.MODERATOR))
        val userCookie = ensureCookieNotNull(cookie = getCookie(Role.USER))

        val input = DeviceInputDTO(
            name = "Test",
            description = "Test",
            streamURL = VALID_STREAM_URL
        )

        val id = createDevice(moderatorCookie, input).id

        deleteDevice(moderatorCookie, id)
        deleteDevice(userCookie, id)
    }

    /**
     * This test ensures that all the roles have access to use GET endpoints
     */
    @Test
    fun `GET endpoints are accessible to all roles`() {
        val roles = Role.values()
        roles.forEach { role ->
            val cookie = ensureCookieNotNull(cookie = getCookie(role))
            mockMvc.perform(
                MockMvcRequestBuilders.get("/devices")
                    .cookie(cookie)
            ).andExpect(MockMvcResultMatchers.status().isOk)
        }
    }

    fun createDevice(cookie: Cookie, input: DeviceInputDTO): IDOutput {
        val inputJson = mapper.writeValueAsString(input)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(inputJson)
                .cookie(cookie)
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andReturn().response.contentAsString

        return mapper.readValue(result, IDOutput::class.java)
    }

    fun deleteDevice(cookie: Cookie, id: ID) {
        mockMvc.request<DeviceInputDTO, ProblemDetail>(
            method = HTTPMethod.DELETE,
            uri = "/devices?deviceIDs=$id",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isForbidden)
            }
        )
    }

    private fun getCookie(role: Role = Role.USER, emailCounter: Int = counter++): Cookie? {
        val inputLogin = createTestUser(userService, role, emailCounter)
        val loginJson = mapper.writeValueAsString(inputLogin)

        val loginResult = mockMvc.perform(
            MockMvcRequestBuilders.post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson)
        ).andExpect(MockMvcResultMatchers.status().isOk)

        return loginResult
            .andReturn()
            .response
            .getCookie(Constants.User.AUTH_COOKIE_NAME)
    }
}
