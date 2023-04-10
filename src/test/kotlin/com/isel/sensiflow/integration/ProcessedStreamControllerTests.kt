package com.isel.sensiflow.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.isel.sensiflow.Constants
import com.isel.sensiflow.http.entities.output.IDOutput
import com.isel.sensiflow.model.dao.ProcessedStream
import com.isel.sensiflow.model.repository.DeviceRepository
import com.isel.sensiflow.model.repository.ProcessedStreamRepository
import com.isel.sensiflow.services.Role
import com.isel.sensiflow.services.UserService
import com.isel.sensiflow.services.dto.input.DeviceInputDTO
import com.isel.sensiflow.services.dto.output.ProcessedStreamExpandedOutputDTO
import com.isel.sensiflow.services.dto.output.ProcessedStreamSimpleOutputDTO
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(org.springframework.test.context.junit4.SpringRunner::class)
@AutoConfigureMockMvc
@Transactional
class ProcessedStreamControllerTests {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var processedStreamRepository: ProcessedStreamRepository

    @Autowired
    lateinit var deviceRepository: DeviceRepository

    @Autowired
    lateinit var userService: UserService

    companion object {
        val mapper: ObjectMapper = jacksonObjectMapper()
    }

    @Test
    fun `get a simple processed stream of a device successfully`() {
        val cookie = ensureCookieNotNull(cookie = getCookie())

        val id = createDevice(
            cookie,
            DeviceInputDTO(
                name = "Test",
                description = "Test",
                streamURL = "Test.url"
            )
        )?.id ?: fail("Failed to create device")

        val processedStreams = processedStreamRepository.findAll()
        val processedStreamID = if (processedStreams.isEmpty()) 1 else processedStreams.last().id + 1
        val processedStream = ProcessedStream(
            id = processedStreamID,
            streamURL = "Test.url",
            device = deviceRepository.findById(id).get()
        )

        processedStreamRepository.save(processedStream)

        mockMvc.request<Unit, ProcessedStreamSimpleOutputDTO>(
            method = HTTPMethod.GET,
            uri = "/devices/$id/processed-stream?expanded=false",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.deviceID").value(id))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.streamUrl").value("Test.url"))
            }
        )
    }

    @Test
    fun `get an expanded processed stream of a device successfully`() {
        val cookie = ensureCookieNotNull(cookie = getCookie())

        val device = createDevice(
            cookie,
            DeviceInputDTO(
                name = "Test",
                description = "Test",
                streamURL = "Test.url"
            )
        )

        val id = device?.id ?: fail("Device not created")

        val processedStreams = processedStreamRepository.findAll()
        val processedStreamID = if (processedStreams.isEmpty()) 1 else processedStreams.last().id + 1
        val processedStream = ProcessedStream(
            id = processedStreamID,
            streamURL = "Test.url",
            device = deviceRepository.findById(id).get()
        )

        processedStreamRepository.save(processedStream)

        mockMvc.request<Unit, ProcessedStreamExpandedOutputDTO>(
            method = HTTPMethod.GET,
            uri = "/devices/$id/processed-stream?expanded=true",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.deviceID").doesNotExist())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.streamUrl").value("Test.url"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.device.id").value(id))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.device.name").value("Test"))
            }
        )
    }

    @Test
    fun `get a processed stream of a device that does not exist`() {
        val cookie = ensureCookieNotNull(cookie = getCookie())

        mockMvc.request<Unit, ProblemDetail>(
            method = HTTPMethod.GET,
            uri = "/devices/-1/processed-stream?expanded=false",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isNotFound)
            }
        )
    }

    private fun createDevice(cookie: Cookie, input: DeviceInputDTO): IDOutput? {
        return mockMvc.request<DeviceInputDTO, IDOutput>(
            method = HTTPMethod.POST,
            uri = "/devices",
            body = input,
            authorization = cookie,
            mapper = DeviceControllerTests.mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isCreated)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
            }
        )
    }

    private fun getCookie(): Cookie? {
        val inputLogin = createTestUser(userService, Role.OWNER)
        val loginJson = DevicesGroupControllerTests.mapper.writeValueAsString(inputLogin)

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
