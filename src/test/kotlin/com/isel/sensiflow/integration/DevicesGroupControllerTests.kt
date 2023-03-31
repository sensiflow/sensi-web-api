package com.isel.sensiflow.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.isel.sensiflow.Constants.User.AUTH_COOKIE_NAME
import com.isel.sensiflow.http.controller.RequestPaths
import com.isel.sensiflow.http.entities.input.UserRegisterInput
import com.isel.sensiflow.services.dto.input.DeviceInputDTO
import com.isel.sensiflow.services.dto.input.DevicesGroupCreateDTO
import com.isel.sensiflow.services.dto.input.DevicesGroupInputDTO
import com.isel.sensiflow.services.dto.input.DevicesGroupUpdateDTO
import com.isel.sensiflow.services.dto.output.DeviceGroupOutputDTO
import com.isel.sensiflow.services.dto.output.DeviceOutputDTO
import com.isel.sensiflow.services.dto.output.IDOutputDTO
import com.isel.sensiflow.services.dto.output.PageDTO
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(org.springframework.test.context.junit4.SpringRunner::class)
@AutoConfigureMockMvc
@Transactional
class DevicesGroupControllerTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    companion object {
        val mapper = jacksonObjectMapper()
        data class RandomInput(val random: String)
    }

    @Test
    fun `update a group successfully`() {
        val cookie = get(cookie = createUser())

        val createResponse = mockMvc.request<DevicesGroupCreateDTO, IDOutputDTO>(
            method = HTTPMethod.POST,
            uri = "/groups",
            body = DevicesGroupCreateDTO("Test", "Test"),
            cookie = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isCreated)
                    .andExpect(jsonPath("$.id").exists())
            }
        )
        val id = createResponse?.id ?: fail("Failed to create test group")

        mockMvc.request<DevicesGroupUpdateDTO, Unit>(
            method = HTTPMethod.PUT,
            uri = "/groups/$id",
            body = DevicesGroupUpdateDTO("Test2", "Test2"),
            cookie = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNoContent)
            }
        )

        mockMvc.request<Unit, DeviceGroupOutputDTO>(
            method = HTTPMethod.GET,
            uri = "/groups/$id",
            cookie = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isOk)
                    .andExpect(jsonPath("$.name").value("Test2"))
                    .andExpect(jsonPath("$.description").value("Test2"))
            }
        )
    }

    @Test
    fun `update a group with an invalid body returns Bad Request`() {
        val cookie = get(cookie = createUser())
        val createResponse = createTestGroup(cookie)
        val id = createResponse?.id ?: fail("Failed to create test group")

        mockMvc.request<RandomInput, Unit>(
            method = HTTPMethod.PUT,
            uri = "/groups/$id",
            body = RandomInput("Test"),
            cookie = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isBadRequest)
            }
        )
    }

    @Test
    fun `update a group that does not exist returns Not Found`() {
        val cookie = get(cookie = createUser())
        /** TODO: DO ERROR HANDLING
         mockMvc.request<DevicesGroupUpdateDTO, Unit>(
         method = HTTPMethod.PUT,
         uri = "/groups/-1",
         body = DevicesGroupUpdateDTO("Test2", "Test2"),
         cookie = cookie,
         mapper = mapper,
         assertions = {
         andExpect(status().isNotFound)
         }
         )*/
    }

    @Test
    fun `delete a group successfully`() {
        val cookie = get(cookie = createUser())
        val createResponse = createTestGroup(cookie)
        val id = createResponse?.id ?: fail("Failed to create test group")

        mockMvc.request<IDOutputDTO, Unit>(
            method = HTTPMethod.DELETE,
            uri = "/groups/$id",
            cookie = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNoContent)
            }
        )

        // TODO: Get the group and check if it was deleted
    }

    @Test
    fun `delete a group that does not exist returns Not Found`() {
        val cookie = get(cookie = createUser())
        /** TODO: DO ERROR HANDLING
         mockMvc.request<IDOutputDTO, Unit>(
         method = HTTPMethod.DELETE,
         uri = "/groups/-1",
         cookie = cookie,
         mapper = mapper,
         assertions = {
         andExpect(status().isNotFound)
         }
         )
         */
    }

    @Test
    fun `edit and get the group's devices successfully`() {
        val cookie = get(cookie = createUser())
        val createResponse = createTestGroup(cookie)
        val id = createResponse?.id ?: fail("Failed to create test group")

        val responseDevice1 = createDevice(
            cookie,
            DeviceInputDTO(
                name = "Test",
                description = "Test",
                streamURL = "Test.url"
            )
        )

        val responseDevice2 = createDevice(
            cookie,
            DeviceInputDTO(
                name = "Test2",
                description = "Test2",
                streamURL = "Test2.url"
            )
        )

        val id1 = responseDevice1?.id ?: fail("Failed to create test device")
        val id2 = responseDevice2?.id ?: fail("Failed to create test device")

        mockMvc.request<DevicesGroupInputDTO, Unit>(
            method = HTTPMethod.PUT,
            uri = "/groups/$id/devices",
            body = DevicesGroupInputDTO(listOf(id1, id2)),
            cookie = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNoContent)
            }
        )

        mockMvc.request<Unit, PageDTO<DeviceOutputDTO>>(
            method = HTTPMethod.GET,
            uri = "/groups/$id/devices?page=0&size=10",
            mapper = mapper,
            assertions = {
                andExpect(status().isOk)
                    .andExpect(jsonPath("$.items").isArray)
                    .andExpect(jsonPath("$.items[0].id").value(id1))
                    .andExpect(jsonPath("$.items[1].id").value(id2))
            }
        )
    }

    @Test
    fun `edit the devices of a group that does not exist returns Not Found`() {
        val cookie = get(cookie = createUser())

        val responseDevice1 = createDevice(
            cookie,
            DeviceInputDTO(
                name = "Test",
                description = "Test",
                streamURL = "Test.url"
            )
        )

        val responseDevice2 = createDevice(
            cookie,
            DeviceInputDTO(
                name = "Test2",
                description = "Test2",
                streamURL = "Test2.url"
            )
        )

        val id1 = responseDevice1?.id ?: fail("Failed to create test device")
        val id2 = responseDevice2?.id ?: fail("Failed to create test device")
        /** TODO: DO ERROR HANDLING
         mockMvc.request<DevicesGroupInputDTO, Unit>(
         method = HTTPMethod.PUT,
         uri = "/groups/-1/devices",
         body = DevicesGroupInputDTO(listOf(id1, id2)),
         cookie = cookie,
         mapper = mapper,
         assertions = {
         andExpect(status().isNotFound)
         }
         )*/
    }

    @Test
    fun `edit the devices of a group with an invalid body returns Bad Request`() {
        val cookie = get(cookie = createUser())
        val createResponse = createTestGroup(cookie)
        val id = createResponse?.id ?: fail("Failed to create test group")

        mockMvc.request<RandomInput, Unit>(
            method = HTTPMethod.PUT,
            uri = "/groups/$id/devices",
            body = RandomInput("Test"),
            cookie = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isBadRequest)
            }
        )
    }

    @Test
    fun `get devices from a group that does not exist returns Not Found`() {
        /** TODO: DO ERROR HANDLING
         mockMvc.request<Unit, PageDTO<DeviceOutputDTO>>(
         method = HTTPMethod.GET,
         uri = "/groups/-1/devices?page=0&size=10",
         mapper = mapper,
         assertions = {
         andExpect(status().isNotFound)
         }
         )*/
    }

    private fun createDevice(cookie: Cookie, input: DeviceInputDTO): IDOutputDTO? {
        return mockMvc.request<DeviceInputDTO, IDOutputDTO>(
            method = HTTPMethod.POST,
            uri = "/devices",
            body = input,
            cookie = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isCreated)
                    .andExpect(jsonPath("$.id").exists())
            }
        )
    }

    private fun createTestGroup(cookie: Cookie): IDOutputDTO? {
        return mockMvc.request<DevicesGroupCreateDTO, IDOutputDTO>(
            method = HTTPMethod.POST,
            uri = "/groups",
            body = DevicesGroupCreateDTO("Test", "Test"),
            cookie = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isCreated)
                    .andExpect(jsonPath("$.id").exists())
            }
        )
    }

    private fun createUser(): Cookie? {
        val user = UserRegisterInput(
            email = "test@email.com",
            firstName = "Test",
            lastName = "Test",
            password = "Password1_"
        )

        val json = mapper.writeValueAsString(user)

        val result = mockMvc.perform(
            post(RequestPaths.Users.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isCreated)

        return result
            .andReturn()
            .response
            .getCookie(AUTH_COOKIE_NAME)
    }
}
