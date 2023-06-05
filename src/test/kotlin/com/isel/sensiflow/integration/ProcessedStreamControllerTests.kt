package com.isel.sensiflow.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.isel.sensiflow.Constants
import com.isel.sensiflow.http.controller.RequestPaths
import com.isel.sensiflow.http.entities.output.IDOutput
import com.isel.sensiflow.model.entities.ProcessedStream
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
        private val mapper: ObjectMapper = jacksonObjectMapper()
        private var counter = 0
        private const val VALID_STREAM_URL = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov"
    }

    @Test
    fun `get a simple processed stream of a device successfully`() {
        val cookie = ensureCookieNotNull(cookie = getCookie())

        val id = createDevice(
            cookie,
            DeviceInputDTO(
                name = "Test",
                description = "Test",
                streamURL = VALID_STREAM_URL
            )
        )?.id ?: fail("Failed to create device")

        val processedStreams = processedStreamRepository.findAll()
        val processedStreamID = if (processedStreams.isEmpty()) 1 else processedStreams.last().id + 1
        val processedStream = ProcessedStream(
            id = processedStreamID,
            processedStreamURL = VALID_STREAM_URL,
            device = deviceRepository.findById(id).get()
        )

        processedStreamRepository.save(processedStream)

        mockMvc.request<Unit, ProcessedStreamSimpleOutputDTO>(
            method = HTTPMethod.GET,
            uri = RequestPaths.Root.ROOT + "/devices/$id/processed-stream?expanded=false",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.deviceID").value(id))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.processedStreamUrl").value(VALID_STREAM_URL))
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
                streamURL = VALID_STREAM_URL
            )
        )

        val id = device?.id ?: fail("Device not created")

        val processedStreams = processedStreamRepository.findAll()
        val processedStreamID = if (processedStreams.isEmpty()) 1 else processedStreams.last().id + 1
        val processedStream = ProcessedStream(
            id = processedStreamID,
            processedStreamURL = VALID_STREAM_URL,
            device = deviceRepository.findById(id).get()
        )

        processedStreamRepository.save(processedStream)

        mockMvc.request<Unit, ProcessedStreamExpandedOutputDTO>(
            method = HTTPMethod.GET,
            uri = RequestPaths.Root.ROOT + "/devices/$id/processed-stream?expanded=true",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.deviceID").doesNotExist())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.processedStreamUrl").value(VALID_STREAM_URL))
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
            uri = RequestPaths.Root.ROOT + "/devices/-1/processed-stream?expanded=false",
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
            uri = RequestPaths.Root.ROOT +  "/devices",
            body = input,
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isCreated)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
            }
        )
    }

    private fun getCookie(emailCounter: Int = counter++): Cookie? {
        val inputLogin = createTestUser(userService, Role.ADMIN, emailCounter)
        val loginJson = mapper.writeValueAsString(inputLogin)

        val loginResult = mockMvc.perform(
            MockMvcRequestBuilders.post(RequestPaths.Root.ROOT + "/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson)
        ).andExpect(MockMvcResultMatchers.status().isOk)

        return loginResult
            .andReturn()
            .response
            .getCookie(Constants.User.AUTH_COOKIE_NAME)
    }
}
