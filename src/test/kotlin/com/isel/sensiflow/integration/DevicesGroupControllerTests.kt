package com.isel.sensiflow.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.isel.sensiflow.Constants
import com.isel.sensiflow.Constants.User.AUTH_COOKIE_NAME
import com.isel.sensiflow.http.entities.output.IDOutput
import com.isel.sensiflow.services.Role
import com.isel.sensiflow.services.Role.ADMIN
import com.isel.sensiflow.services.Role.USER
import com.isel.sensiflow.services.UserService
import com.isel.sensiflow.services.dto.input.DeviceInputDTO
import com.isel.sensiflow.services.dto.input.DevicesGroupCreateDTO
import com.isel.sensiflow.services.dto.input.DevicesGroupInputDTO
import com.isel.sensiflow.services.dto.input.DevicesGroupUpdateDTO
import com.isel.sensiflow.services.dto.output.AddDevicesToGroupOutputDTO
import com.isel.sensiflow.services.dto.output.DeviceGroupOutputExpandedDTO
import com.isel.sensiflow.services.dto.output.DeviceGroupSimpleOutputDTO
import com.isel.sensiflow.services.dto.output.PageDTO
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
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

    @Autowired
    lateinit var userService: UserService

    companion object {
        private val mapper = jacksonObjectMapper()
        private var counter = 0
        private const val VALID_STREAM_URL = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov"
        data class RandomInput(val random: String)
    }

    @Test
    fun `update a group successfully`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = Role.MODERATOR))

        val createResponse = mockMvc.request<DevicesGroupCreateDTO, IDOutput>(
            method = HTTPMethod.POST,
            uri = "/groups",
            body = DevicesGroupCreateDTO("Test", "Test"),
            authorization = cookie,
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
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNoContent)
            }
        )

        mockMvc.request<Unit, DeviceGroupSimpleOutputDTO>(
            method = HTTPMethod.GET,
            uri = "/groups/$id",
            authorization = cookie,
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
        val cookie = ensureCookieNotNull(cookie = getCookie(role = Role.MODERATOR))
        val createResponse = createTestGroup(cookie)
        val id = createResponse?.id ?: fail("Failed to create test group")

        mockMvc.request<RandomInput, ProblemDetail>(
            method = HTTPMethod.PUT,
            uri = "/groups/$id",
            body = RandomInput("BadRequest"),
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isBadRequest)
            }
        )
    }

    @Test
    fun `updating a group with insufficient permissions returns Forbidden`() {
        val userCookie = ensureCookieNotNull(cookie = getCookie(role = USER))
        val moderatorCookie = ensureCookieNotNull(cookie = getCookie(role = Role.MODERATOR))
        val createResponse = createTestGroup(moderatorCookie)
        val id = createResponse?.id ?: fail("Failed to create test group")

        mockMvc.request<DevicesGroupUpdateDTO, ProblemDetail>(
            method = HTTPMethod.PUT,
            uri = "/groups/$id",
            body = DevicesGroupUpdateDTO("Test2", "Test2"),
            authorization = userCookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isForbidden)
            }
        )
    }

    @Test
    fun `update a group that does not exist returns Not Found`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = Role.MODERATOR))

        mockMvc.request<DevicesGroupUpdateDTO, ProblemDetail>(
            method = HTTPMethod.PUT,
            uri = "/groups/-1",
            body = DevicesGroupUpdateDTO("Test2", "Test2"),
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNotFound)
            }
        )
    }

    @Test
    fun `delete a group successfully`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = ADMIN))
        val createResponse = createTestGroup(cookie)
        val id = createResponse?.id ?: fail("Failed to create test group")

        mockMvc.request<Unit, Unit>(
            method = HTTPMethod.DELETE,
            uri = "/groups/$id",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNoContent)
            }
        )

        mockMvc.request<Unit, ProblemDetail>(
            method = HTTPMethod.GET,
            uri = "/groups/$id",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNotFound)
            }
        )
    }

    @Test
    fun `deleting a group with insufficient permissions returns Forbidden`() {
        val userCookie = ensureCookieNotNull(cookie = getCookie(role = USER))
        val moderatorCookie = ensureCookieNotNull(cookie = getCookie(role = Role.MODERATOR))
        val createResponse = createTestGroup(moderatorCookie)
        val id = createResponse?.id ?: fail("Failed to create test group")

        mockMvc.request<Unit, ProblemDetail>(
            method = HTTPMethod.DELETE,
            uri = "/groups/$id",
            authorization = userCookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isForbidden)
            }
        )
    }

    @Test
    fun `delete a group that does not exist returns Not Found`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = ADMIN))
        mockMvc.request<Unit, ProblemDetail>(
            method = HTTPMethod.DELETE,
            uri = "/groups/-1",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNotFound)
            }
        )
    }

    @Test
    fun `try to edit a group's description only`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = Role.MODERATOR))
        val createResponse = createTestGroup(cookie)
        val id = createResponse?.id ?: fail("Failed to create test group")

        mockMvc.request<DevicesGroupUpdateDTO, Unit>(
            method = HTTPMethod.PUT,
            uri = "/groups/$id",
            body = DevicesGroupUpdateDTO(description = "Test2"),
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNoContent)
            }
        )

        mockMvc.request<Unit, DeviceGroupSimpleOutputDTO>(
            method = HTTPMethod.GET,
            uri = "/groups/$id",
            mapper = mapper,
            authorization = cookie,
            assertions = {
                andExpect(status().isOk)
                    .andExpect(jsonPath("$.name").value("Test"))
                    .andExpect(jsonPath("$.description").value("Test2"))
            }
        )
    }

    @Test
    fun `try to edit the groups name and cleaning description`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = Role.MODERATOR))
        val createResponse = createTestGroup(cookie)
        val id = createResponse?.id ?: fail("Failed to create test group")

        mockMvc.request<DevicesGroupUpdateDTO, Unit>(
            method = HTTPMethod.PUT,
            uri = "/groups/$id",
            body = DevicesGroupUpdateDTO(name = "changed", description = ""),
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNoContent)
            }
        )

        mockMvc.request<Unit, DeviceGroupSimpleOutputDTO>(
            method = HTTPMethod.GET,
            uri = "/groups/$id",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isOk)
                    .andExpect(jsonPath("$.name").value("changed"))
                    .andExpect(jsonPath("$.description").value(""))
            }
        )
    }

    @Test
    fun `try to update a group with a non existent device`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = Role.MODERATOR))

        mockMvc.request<DevicesGroupCreateDTO, ProblemDetail>(
            method = HTTPMethod.POST,
            uri = "/groups?devices=4,200",
            body = DevicesGroupCreateDTO("Test", "Test"),
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNotFound)
                    .andExpect(
                        jsonPath("$.type")
                            .value(Constants.Problem.URI.DEVICE_NOT_FOUND)
                    )
            }
        )
    }

    @Test
    fun `get devices from a group that does not exist returns Not Found`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = USER))

        mockMvc.request<Unit, ProblemDetail>(
            method = HTTPMethod.GET,
            uri = "/groups/-1/devices?page=0&size=10",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNotFound)
            }
        )
    }

    @Test
    fun `create a device group successfully`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = Role.MODERATOR))

        mockMvc.request<DevicesGroupCreateDTO, IDOutput>(
            method = HTTPMethod.POST,
            uri = "/groups",
            body = DevicesGroupCreateDTO("Test", "Test"),
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isCreated)
                    .andExpect(jsonPath("$.id").exists())
            }
        )
    }

    @Test
    fun `try to create a group without a description`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = Role.MODERATOR))

        val responseGroup = mockMvc.request<DevicesGroupCreateDTO, IDOutput>(
            method = HTTPMethod.POST,
            uri = "/groups",
            body = DevicesGroupCreateDTO("Test", null),
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isCreated)
                    .andExpect(jsonPath("$.id").exists())
            }
        )

        val id = responseGroup?.id ?: fail("Failed to create test device")

        mockMvc.request<Unit, DeviceGroupSimpleOutputDTO>(
            method = HTTPMethod.GET,
            uri = "/groups/$id",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isOk)
                    .andExpect(jsonPath("$.description").isEmpty)
                    .andExpect(jsonPath("$.name").value("Test"))
            }
        )
    }

    data class BadInput(val description: String)

    @Test
    fun `try to create a group without a name`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = Role.MODERATOR))
        val input = BadInput(description = "adwdw")

        mockMvc.request<BadInput, ProblemDetail>(
            method = HTTPMethod.POST,
            uri = "/groups",
            body = input,
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isBadRequest)
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value(Constants.Problem.URI.INVALID_JSON_BODY))
            }
        )
    }

    @Test
    fun `create a group and try to add invalid devices`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = Role.MODERATOR))

        mockMvc.request<DevicesGroupCreateDTO, ProblemDetail>(
            method = HTTPMethod.POST,
            uri = "/groups?devices=4,200",
            body = DevicesGroupCreateDTO("Test", "Test"),
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNotFound)
                    .andExpect(
                        jsonPath("$.type")
                            .value(Constants.Problem.URI.DEVICE_NOT_FOUND)
                    )
            }
        )
    }

    @Test
    fun `create a group and add devices successfully`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = Role.MODERATOR))

        val id1 = createDevice(
            cookie,
            DeviceInputDTO(
                name = "Test",
                description = "Test",
                streamURL = VALID_STREAM_URL
            )
        )

        val id2 = createDevice(
            cookie,
            DeviceInputDTO(
                name = "Test",
                description = "Test",
                streamURL = VALID_STREAM_URL
            )
        )

        mockMvc.request<DevicesGroupCreateDTO, IDOutput>(
            method = HTTPMethod.POST,
            uri = "/groups?devices=${id1?.id},${id2?.id}",
            body = DevicesGroupCreateDTO(
                name = "Test",
                description = "Test"
            ),
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isCreated)
                    .andExpect(jsonPath("$.id").exists())
            }
        )
    }

    @Test
    fun `try to get a group that does not exist`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = USER))

        mockMvc.request<Unit, ProblemDetail>(
            method = HTTPMethod.GET,
            uri = "/groups/-1",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNotFound)
                    .andExpect(
                        jsonPath("$.type")
                            .value(Constants.Problem.URI.DEVICE_GROUP_NOT_FOUND)
                    )
            }
        )
    }

    fun `get a simple group successfully`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = USER))

        val id = mockMvc.request<DevicesGroupCreateDTO, IDOutput>(
            method = HTTPMethod.POST,
            uri = "/groups",
            body = DevicesGroupCreateDTO(
                name = "Test",
                description = "Test"
            ),
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isCreated)
                    .andExpect(jsonPath("$.id").exists())
            }
        )?.id ?: fail("Failed to create test group")

        mockMvc.request<Unit, DeviceGroupSimpleOutputDTO>(
            method = HTTPMethod.GET,
            uri = "/groups/$id",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isOk)
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.name").value("Test"))
                    .andExpect(jsonPath("$.description").value("Test"))
            }
        )
    }

    @Test
    fun `get an expanded group successfully`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = ADMIN))

        val device1 = DeviceInputDTO(
            name = "Test1",
            description = "Test1",
            streamURL = VALID_STREAM_URL
        )

        val device2 = DeviceInputDTO(
            name = "Test2",
            description = "Test2",
            streamURL = VALID_STREAM_URL
        )

        val device1ID = createDevice(cookie, device1)?.id ?: fail("Failed to create test device")
        val device2ID = createDevice(cookie, device2)?.id ?: fail("Failed to create test device")

        val groupID = createTestGroup(cookie)?.id ?: fail("Failed to create test group")

        mockMvc.request<DevicesGroupInputDTO, AddDevicesToGroupOutputDTO>(
            method = HTTPMethod.POST,
            uri = "/groups/$groupID/devices",
            body = DevicesGroupInputDTO(
                deviceIDs = listOf(device1ID, device2ID)
            ),
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isOk)
            }
        )

        mockMvc.request<Unit, DeviceGroupOutputExpandedDTO>(
            method = HTTPMethod.GET,
            uri = "/groups/$groupID?expanded=true",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isOk)
                    .andExpect(jsonPath("$.devices").exists())
                    .andExpect(jsonPath("$.devices.items").exists())
                    .andExpect(jsonPath("$.devices.items[0].id").value(device1ID))
                    .andExpect(jsonPath("$.devices.items[1].id").value(device2ID))
            }
        )
    }

    @Test
    fun `get all groups successfully`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = ADMIN))

        val groupID1 = createTestGroup(cookie)?.id ?: fail("Failed to create test group")
        val groupID2 = createTestGroup(cookie)?.id ?: fail("Failed to create test group")
        val groupID3 = createTestGroup(cookie)?.id ?: fail("Failed to create test group")

        mockMvc.request<Unit, PageDTO<DeviceGroupSimpleOutputDTO>>(
            method = HTTPMethod.GET,
            uri = "/groups",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isOk)
                    .andExpect(jsonPath("$.items").exists())
                    .andExpect(jsonPath("$.items[0].id").value(groupID1))
                    .andExpect(jsonPath("$.items[1].id").value(groupID2))
                    .andExpect(jsonPath("$.items[2].id").value(groupID3))
            }
        )
    }

    @Test
    fun `get all groups expanded successfully`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = ADMIN))

        val device1 = DeviceInputDTO(
            name = "Test1",
            description = "Test1",
            streamURL = VALID_STREAM_URL
        )

        val device2 = DeviceInputDTO(
            name = "Test2",
            description = "Test2",
            streamURL = VALID_STREAM_URL
        )

        val device1ID = createDevice(cookie, device1)?.id ?: fail("Failed to create test device")
        val device2ID = createDevice(cookie, device2)?.id ?: fail("Failed to create test device")

        val groupID1 = createTestGroup(cookie)?.id ?: fail("Failed to create test group")
        val groupID2 = createTestGroup(cookie)?.id ?: fail("Failed to create test group")
        val groupID3 = createTestGroup(cookie)?.id ?: fail("Failed to create test group")

        mockMvc.request<DevicesGroupInputDTO, AddDevicesToGroupOutputDTO>(
            method = HTTPMethod.POST,
            uri = "/groups/$groupID1/devices",
            body = DevicesGroupInputDTO(
                deviceIDs = listOf(device1ID, device2ID)
            ),
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isOk)
            }
        )

        mockMvc.request<DevicesGroupInputDTO, AddDevicesToGroupOutputDTO>(
            method = HTTPMethod.POST,
            uri = "/groups/$groupID2/devices",
            body = DevicesGroupInputDTO(
                deviceIDs = listOf(device1ID, device2ID)
            ),
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isOk)
            }
        )

        mockMvc.request<DevicesGroupInputDTO, AddDevicesToGroupOutputDTO>(
            method = HTTPMethod.POST,
            uri = "/groups/$groupID3/devices",
            body = DevicesGroupInputDTO(
                deviceIDs = listOf(device1ID, device2ID)
            ),
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isOk)
            }
        )

        mockMvc.request<Unit, PageDTO<DeviceGroupOutputExpandedDTO>>(
            method = HTTPMethod.GET,
            uri = "/groups?expanded=true",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isOk)
                    .andExpect(jsonPath("$.items").exists())
                    .andExpect(jsonPath("$.items[0].id").value(groupID1))
                    .andExpect(jsonPath("$.items[1].id").value(groupID2))
                    .andExpect(jsonPath("$.items[2].id").value(groupID3))
                    .andExpect(jsonPath("$.items[0].devices").exists())
                    .andExpect(jsonPath("$.items[0].devices.items").exists())
                    .andExpect(jsonPath("$.items[1].devices").exists())
                    .andExpect(jsonPath("$.items[1].devices.items").exists())
                    .andExpect(jsonPath("$.items[2].devices").exists())
                    .andExpect(jsonPath("$.items[2].devices.items").exists())
            }
        )
    }

    @Test
    fun `add and delete devices from a group successfully`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = ADMIN))

        val device1 = DeviceInputDTO(
            name = "Test1",
            description = "Test1",
            streamURL = VALID_STREAM_URL
        )

        val device2 = DeviceInputDTO(
            name = "Test2",
            description = "Test2",
            streamURL = VALID_STREAM_URL
        )

        val device1ID = createDevice(cookie, device1)?.id ?: fail("Failed to create test device")
        val device2ID = createDevice(cookie, device2)?.id ?: fail("Failed to create test device")

        val groupID = createTestGroup(cookie)?.id ?: fail("Failed to create test group")

        mockMvc.request<DevicesGroupInputDTO, AddDevicesToGroupOutputDTO>(
            method = HTTPMethod.POST,
            uri = "/groups/$groupID/devices",
            body = DevicesGroupInputDTO(
                deviceIDs = listOf(device1ID, device2ID)
            ),
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isOk)
            }
        )

        mockMvc.request<Unit, PageDTO<DeviceGroupOutputExpandedDTO>>(
            method = HTTPMethod.GET,
            uri = "/groups?expanded=true",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isOk)
                    .andExpect(jsonPath("$.items").exists())
                    .andExpect(jsonPath("$.items[0].id").value(groupID))
                    .andExpect(jsonPath("$.items[0].devices").exists())
                    .andExpect(jsonPath("$.items[0].devices.items").exists())
                    .andExpect(jsonPath("$.items[0].devices.items[0].id").exists())
                    .andExpect(jsonPath("$.items[0].devices.items[1].id").exists())
            }
        )

        mockMvc.request<Unit, Unit>(
            method = HTTPMethod.DELETE,
            uri = "/groups/$groupID/devices?deviceIDs=$device1ID",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNoContent)
            }
        )

        mockMvc.request<Unit, PageDTO<DeviceGroupOutputExpandedDTO>>(
            method = HTTPMethod.GET,
            uri = "/groups?expanded=true",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isOk)
                    .andExpect(jsonPath("$.items").exists())
                    .andExpect(jsonPath("$.items[0].id").value(groupID))
                    .andExpect(jsonPath("$.items[0].devices").exists())
                    .andExpect(jsonPath("$.items[0].devices.items").exists())
                    .andExpect(jsonPath("$.items[0].devices.items[0].id").exists())
                    .andExpect(jsonPath("$.items[0].devices.items[1].id").doesNotExist())
            }
        )
    }

    @Test
    fun `add a device to a group that does not exist`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = ADMIN))

        val device1 = DeviceInputDTO(
            name = "Test1",
            description = "Test1",
            streamURL = VALID_STREAM_URL
        )

        val device1ID = createDevice(cookie, device1)?.id ?: fail("Failed to create test device")

        mockMvc.request<DevicesGroupInputDTO, ProblemDetail>(
            method = HTTPMethod.POST,
            uri = "/groups/-1/devices",
            body = DevicesGroupInputDTO(
                deviceIDs = listOf(device1ID)
            ),
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNotFound)
            }
        )
    }

    @Test
    fun `add a device that does not exist to a group`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = ADMIN))

        val groupID = createTestGroup(cookie)?.id ?: fail("Failed to create test group")

        mockMvc.request<DevicesGroupInputDTO, ProblemDetail>(
            method = HTTPMethod.POST,
            uri = "/groups/$groupID/devices",
            body = DevicesGroupInputDTO(
                deviceIDs = listOf(-1)
            ),
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNotFound)
            }
        )
    }

    @Test
    fun `add a device that already was added to a group`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = ADMIN))

        val device1 = DeviceInputDTO(
            name = "Test1",
            description = "Test1",
            streamURL = VALID_STREAM_URL
        )

        val device1ID = createDevice(cookie, device1)?.id ?: fail("Failed to create test device")

        val groupID = createTestGroup(cookie)?.id ?: fail("Failed to create test group")

        mockMvc.request<DevicesGroupInputDTO, AddDevicesToGroupOutputDTO>(
            method = HTTPMethod.POST,
            uri = "/groups/$groupID/devices",
            body = DevicesGroupInputDTO(
                deviceIDs = listOf(device1ID)
            ),
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isOk)
            }
        )

        mockMvc.request<DevicesGroupInputDTO, ProblemDetail>(
            method = HTTPMethod.POST,
            uri = "/groups/$groupID/devices",
            body = DevicesGroupInputDTO(
                deviceIDs = listOf(device1ID)
            ),
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isBadRequest)
            }
        )
    }

    @Test
    fun `delete a device from a group that does not exist`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = ADMIN))

        val device1 = DeviceInputDTO(
            name = "Test1",
            description = "Test1",
            streamURL = VALID_STREAM_URL
        )

        val device1ID = createDevice(cookie, device1)?.id ?: fail("Failed to create test device")

        mockMvc.request<Unit, ProblemDetail>(
            method = HTTPMethod.DELETE,
            uri = "/groups/-1/devices?deviceIDs=$device1ID",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNotFound)
            }
        )
    }

    @Test
    fun `delete a device that does not exist from a group`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = ADMIN))

        val groupID = createTestGroup(cookie)?.id ?: fail("Failed to create test group")

        mockMvc.request<Unit, ProblemDetail>(
            method = HTTPMethod.DELETE,
            uri = "/groups/$groupID/devices?deviceIDs=-1",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isNotFound)
            }
        )
    }

    private fun createDevice(cookie: Cookie, input: DeviceInputDTO): IDOutput? {
        return mockMvc.request<DeviceInputDTO, IDOutput>(
            method = HTTPMethod.POST,
            uri = "/devices",
            body = input,
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isCreated)
                    .andExpect(jsonPath("$.id").exists())
            }
        )
    }

    private fun createTestGroup(cookie: Cookie): IDOutput? {
        return mockMvc.request<DevicesGroupCreateDTO, IDOutput>(
            method = HTTPMethod.POST,
            uri = "/groups",
            body = DevicesGroupCreateDTO("Test", "Test"),
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(status().isCreated)
                    .andExpect(jsonPath("$.id").exists())
            }
        )
    }

    private fun getCookie(role: Role = USER, emailCounter: Int = counter++): Cookie? {
        val inputLogin = createTestUser(userService, role, emailCounter)
        val loginJson = mapper.writeValueAsString(inputLogin)

        val loginResult = mockMvc.perform(
            post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson)
        ).andExpect(status().isOk)

        return loginResult
            .andReturn()
            .response
            .getCookie(AUTH_COOKIE_NAME)
    }
}
