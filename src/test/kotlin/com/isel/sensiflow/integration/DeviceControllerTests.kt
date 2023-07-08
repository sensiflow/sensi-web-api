package com.isel.sensiflow.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.isel.sensiflow.Constants
import com.isel.sensiflow.http.controller.RequestPaths
import com.isel.sensiflow.http.entities.output.IDOutput
import com.isel.sensiflow.model.entities.Metric
import com.isel.sensiflow.model.entities.MetricID
import com.isel.sensiflow.model.repository.DeviceRepository
import com.isel.sensiflow.model.repository.MetricRepository
import com.isel.sensiflow.services.Role
import com.isel.sensiflow.services.beans.UserService
import com.isel.sensiflow.services.dto.input.DeviceInputDTO
import com.isel.sensiflow.services.dto.input.DeviceUpdateDTO
import com.isel.sensiflow.services.dto.input.DevicesGroupCreateDTO
import com.isel.sensiflow.services.dto.output.DeviceOutputDTO
import com.isel.sensiflow.services.dto.output.DeviceSimpleOutputDTO
import com.isel.sensiflow.services.dto.output.MetricOutputDTO
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(org.springframework.test.context.junit4.SpringRunner::class)
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class DeviceControllerTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var metricRepository: MetricRepository

    @Autowired
    lateinit var deviceRepository: DeviceRepository

    @Autowired
    lateinit var userService: UserService

    companion object {
        private val mapper = jacksonObjectMapper()
        private var counter = 0
        private const val VALID_STREAM_URL = "rtsp://sensiflow.zapto.org:554/1/stream1"
        private const val INVALID_STREAM_URL = "definitely not a valid url"
    }

    @Test
    fun `get device stats successfully`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = Role.USER))

        val responseDevice1 = createDevice(
            DeviceInputDTO(
                name = "Test",
                description = "Test",
                streamURL = VALID_STREAM_URL
            )
        )

        val id1 = responseDevice1?.id ?: fail("Failed to create test device")

        val startingTimestampMetric1 = Timestamp(System.currentTimeMillis())
        val endingTimestampMetric1 = Timestamp(System.currentTimeMillis() + 10000)

        val startingTimestampMetric2 = Timestamp(System.currentTimeMillis() + 20000)
        val endingTimestampMetric2 = Timestamp(System.currentTimeMillis() + 30000)

        val device = deviceRepository.findById(id1).get()

        metricRepository.save(
            Metric(
                id = MetricID(
                    deviceID = id1,
                    startTime = startingTimestampMetric1
                ),
                endTime = endingTimestampMetric1,
                peopleCount = 1,
                device = device
            )
        )

        metricRepository.save(
            Metric(
                id = MetricID(
                    deviceID = id1,
                    startTime = startingTimestampMetric2
                ),
                endTime = endingTimestampMetric2,
                peopleCount = 5,
                device = device
            )
        )

        mockMvc.request<Unit, PageDTO<MetricOutputDTO>>(
            method = HTTPMethod.GET,
            uri =  "/devices/$id1/stats?page=0&size=10",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.items").isArray)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].peopleCount").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.items[1].peopleCount").value(5))
            }
        )
    }

    @Test
    fun `get the stats of a device that does not exist`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = Role.USER))

        mockMvc.request<Unit, ProblemDetail>(
            method = HTTPMethod.GET,
            uri =  "/devices/-1/stats?page=0&size=10",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isNotFound)
            }
        )
    }

    @Test
    fun `create a valid device`() {
        createDevice(
            input = DeviceInputDTO(
                name = "Test Device",
                description = "Test Description",
                streamURL = VALID_STREAM_URL
            )
        )
    }

    @Test
    fun `create a device with and invalid body gives 400`() {
        val moderatorCookie = ensureCookieNotNull(getCookie(role = Role.MODERATOR))
        mockMvc.request<InvalidBody, ProblemDetail>(
            method = HTTPMethod.POST,
            uri =  "/devices",
            body = InvalidBody(),
            authorization = moderatorCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isBadRequest)
            }
        )
    }

    @Test
    fun `create a device with no name gives 400`() {
        val moderatorCookie = ensureCookieNotNull(getCookie(role = Role.MODERATOR))
        mockMvc.request<DeviceInputDTO, ProblemDetail>(
            method = HTTPMethod.POST,
            uri =  "/devices",
            body = DeviceInputDTO(
                name = "",
                description = "Test Description",
                streamURL = VALID_STREAM_URL
            ),
            authorization = moderatorCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isBadRequest)
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(Constants.Problem.Title.VALIDATION_ERROR))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            }
        )
    }

    @Test
    fun `create a device with no streamUrl gives 400`() {
        val moderatorCookie = ensureCookieNotNull(getCookie(role = Role.MODERATOR))
        mockMvc.request<DeviceInputDTO, ProblemDetail>(
            method = HTTPMethod.POST,
            uri =  "/devices",
            body = DeviceInputDTO(
                name = "name",
                description = "Test Description",
                streamURL = ""
            ),
            authorization = moderatorCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isBadRequest)
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(Constants.Problem.Title.VALIDATION_ERROR))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            }
        )
    }

    @Test
    fun `create a device with a name bigger than max length gives 400`() {
        val moderatorCookie = ensureCookieNotNull(getCookie(role = Role.MODERATOR))
        mockMvc.request<DeviceInputDTO, ProblemDetail>(
            method = HTTPMethod.POST,
            uri =  "/devices",
            body = DeviceInputDTO(
                name = "a".repeat(Constants.Device.NAME_MAX_LENGTH + 1),
                description = "Test Description",
                streamURL = VALID_STREAM_URL
            ),
            authorization = moderatorCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isBadRequest)
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(Constants.Problem.Title.VALIDATION_ERROR))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            }
        )
    }

    @Test
    fun `create a device with a description bigger than max length gives 400`() {
        val moderatorCookie = ensureCookieNotNull(getCookie(role = Role.MODERATOR))
        mockMvc.request<DeviceInputDTO, ProblemDetail>(
            method = HTTPMethod.POST,
            uri =  "/devices",
            body = DeviceInputDTO(
                name = "New Device",
                description = "a".repeat(Constants.Device.DESCRIPTION_MAX_LENGTH + 1),
                streamURL = VALID_STREAM_URL
            ),
            authorization = moderatorCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isBadRequest)
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(Constants.Problem.Title.VALIDATION_ERROR))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            }
        )
    }

    @Test
    fun `create a device with a streamUrl bigger than max length gives 400`() {
        val moderatorCookie = ensureCookieNotNull(getCookie(role = Role.MODERATOR))
        mockMvc.request<DeviceInputDTO, ProblemDetail>(
            method = HTTPMethod.POST,
            uri =  "/devices",
            body = DeviceInputDTO(
                name = "New Device",
                description = "asda",
                streamURL = VALID_STREAM_URL + "a".repeat(Constants.Device.STREAM_URL_MAX_LENGTH + 1)
            ),
            authorization = moderatorCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isBadRequest)
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(Constants.Problem.Title.VALIDATION_ERROR))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            }
        )
    }

    @Test
    fun `create a device with a streamUrl with invalid format gives 400`() {
        val moderatorCookie = ensureCookieNotNull(getCookie(role = Role.MODERATOR))
        mockMvc.request<DeviceInputDTO, ProblemDetail>(
            method = HTTPMethod.POST,
            uri =  "/devices",
            body = DeviceInputDTO(
                name = "New Device",
                description = "asda",
                streamURL = INVALID_STREAM_URL
            ),
            authorization = moderatorCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isBadRequest)
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(Constants.Problem.Title.VALIDATION_ERROR))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            }
        )
    }

    @Test
    fun `create a device with a blank description is allowed`() {
        val moderatorCookie = ensureCookieNotNull(getCookie(role = Role.MODERATOR))
        mockMvc.request<DeviceInputDTO, IDOutput>(
            method = HTTPMethod.POST,
            uri =  "/devices",
            body = DeviceInputDTO(
                name = "New Device",
                description = "",
                streamURL = VALID_STREAM_URL
            ),
            authorization = moderatorCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isCreated)
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber)
            }
        )
    }

    @Test
    fun `create a device without a description is allowed`() {
        val moderatorCookie = ensureCookieNotNull(getCookie(role = Role.MODERATOR))
        mockMvc.request<DeviceInputDTO, IDOutput>(
            method = HTTPMethod.POST,
            uri =  "/devices",
            body = DeviceInputDTO(
                name = "New Device",
                description = null,
                streamURL = VALID_STREAM_URL
            ),
            authorization = moderatorCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isCreated)
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber)
            }
        )
    }

    @Test
    fun `delete a device`() {
        val ADMINCookie = ensureCookieNotNull(getCookie(role = Role.ADMIN))
        val createdDeviceId = createDevice(
            input = DeviceInputDTO(
                name = "Test Device",
                description = "Test Description",
                streamURL = VALID_STREAM_URL
            )
        )

        mockMvc.request<NoBody, NoBody>(
            method = HTTPMethod.DELETE,
            uri =  "/devices?deviceIDs=${createdDeviceId?.id}",
            authorization = ADMINCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isNoContent)
            }
        )

        mockMvc.request<NoBody, ProblemDetail>(
            method = HTTPMethod.GET,
            uri =  "/devices/${createdDeviceId?.id}",
            authorization = ADMINCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isNotFound)
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(Constants.Problem.Title.NOT_FOUND))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404))
            }
        )
    }

    @Test
    fun `delete multiple devices`() {
        val ADMINCookie = ensureCookieNotNull(getCookie(role = Role.ADMIN))
        val createdDeviceId = createDevice(
            input = DeviceInputDTO(
                name = "Test Device",
                description = "Test Description",
                streamURL = VALID_STREAM_URL
            )
        )

        val createdDeviceId2 = createDevice(
            input = DeviceInputDTO(
                name = "Test Device 1",
                description = "Test Description 1",
                streamURL = VALID_STREAM_URL
            )
        )

        val createdDeviceId3 = createDevice(
            input = DeviceInputDTO(
                name = "Test Device 2",
                description = "Test Description 2",
                streamURL = VALID_STREAM_URL
            )
        )

        mockMvc.request<NoBody, DeviceSimpleOutputDTO>(
            method = HTTPMethod.GET,
            uri =  "/devices/${createdDeviceId?.id}",
            authorization = ADMINCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isOk)
            }
        )

        mockMvc.request<NoBody, DeviceSimpleOutputDTO>(
            method = HTTPMethod.GET,
            uri =  "/devices/${createdDeviceId2?.id}",
            authorization = ADMINCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isOk)
            }
        )

        mockMvc.request<NoBody, DeviceSimpleOutputDTO>(
            method = HTTPMethod.GET,
            uri =  "/devices/${createdDeviceId3?.id}",
            authorization = ADMINCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isOk)
            }
        )

        mockMvc.request<NoBody, NoBody>(
            method = HTTPMethod.DELETE,
            uri =  "/devices?deviceIDs=${createdDeviceId?.id}, ${createdDeviceId2?.id}, ${createdDeviceId3?.id}",
            authorization = ADMINCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isNoContent)
            }
        )

        mockMvc.request<NoBody, ProblemDetail>(
            method = HTTPMethod.GET,
            uri =  "/devices/${createdDeviceId?.id}",
            authorization = ADMINCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isNotFound)
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(Constants.Problem.Title.NOT_FOUND))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404))
            }
        )

        mockMvc.request<NoBody, ProblemDetail>(
            method = HTTPMethod.GET,
            uri =  "/devices/${createdDeviceId2?.id}",
            authorization = ADMINCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isNotFound)
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(Constants.Problem.Title.NOT_FOUND))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404))
            }
        )

        mockMvc.request<NoBody, ProblemDetail>(
            method = HTTPMethod.GET,
            uri =  "/devices/${createdDeviceId3?.id}",
            authorization = ADMINCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isNotFound)
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(Constants.Problem.Title.NOT_FOUND))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404))
            }
        )
    }

    @Test
    fun `update a device with empty name gives 400`() {
        val moderatorCookie = ensureCookieNotNull(getCookie(role = Role.MODERATOR))
        val createdDeviceId = createDevice(
            input = DeviceInputDTO(
                name = "Test Device",
                description = "Test Description",
                streamURL = VALID_STREAM_URL
            )
        )

        mockMvc.request<DeviceUpdateDTO, ProblemDetail>(
            method = HTTPMethod.PUT,
            uri =  "/devices/${createdDeviceId?.id}",
            body = DeviceUpdateDTO(
                name = "",
                description = "Test Description",
                streamURL = VALID_STREAM_URL
            ),
            authorization = moderatorCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isBadRequest)
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(Constants.Problem.Title.VALIDATION_ERROR))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            }
        )
    }

    @Test
    fun `update a device with empty description is allowed`() {
        val moderatorCookie = ensureCookieNotNull(getCookie(role = Role.MODERATOR))
        val createdDeviceId = createDevice(
            input = DeviceInputDTO(
                name = "Test Device",
                description = "Test Description",
                streamURL = VALID_STREAM_URL
            )
        )

        mockMvc.request<DeviceUpdateDTO, ProblemDetail>(
            method = HTTPMethod.PUT,
            uri =  "/devices/${createdDeviceId?.id}",
            body = DeviceUpdateDTO(
                name = "New Device",
                description = "",
                streamURL = VALID_STREAM_URL
            ),
            authorization = moderatorCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isNoContent)
            }
        )
    }

    @Test
    fun `update a device with empty stream gives 400`() {
        val moderatorCookie = ensureCookieNotNull(getCookie(role = Role.MODERATOR))
        val createdDeviceId = createDevice(
            input = DeviceInputDTO(
                name = "Test Device",
                description = "Test Description",
                streamURL = VALID_STREAM_URL
            )
        )

        mockMvc.request<DeviceUpdateDTO, ProblemDetail>(
            method = HTTPMethod.PUT,
            uri =  "/devices/${createdDeviceId?.id}",
            body = DeviceUpdateDTO(
                name = "New Device",
                description = "New Description",
                streamURL = ""
            ),
            authorization = moderatorCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isBadRequest)
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(Constants.Problem.Title.VALIDATION_ERROR))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            }
        )
    }

    @Test
    fun `update a device with a stream with invalid format gives 400`() {
        val moderatorCookie = ensureCookieNotNull(getCookie(role = Role.MODERATOR))
        val createdDeviceId = createDevice(
            input = DeviceInputDTO(
                name = "Test Device",
                description = "Test Description",
                streamURL = VALID_STREAM_URL
            )
        )

        mockMvc.request<DeviceUpdateDTO, ProblemDetail>(
            method = HTTPMethod.PUT,
            uri =  "/devices/${createdDeviceId?.id}",
            body = DeviceUpdateDTO(
                name = "New Device",
                description = "New Description",
                streamURL = INVALID_STREAM_URL
            ),
            authorization = moderatorCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isBadRequest)
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(Constants.Problem.Title.VALIDATION_ERROR))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            }
        )
    }

    @Test
    fun `updating only the device's name is allowed`() {

        val moderatorCookie = ensureCookieNotNull(getCookie(role = Role.MODERATOR))

        val deviceId = createDevice(
            input = DeviceInputDTO(
                name = "Test Device",
                description = "Test Description",
                streamURL = VALID_STREAM_URL
            )
        )?.id

        mockMvc.request<DeviceUpdateDTO, ProblemDetail>(
            method = HTTPMethod.PUT,
            uri =  "/devices/$deviceId",
            body = DeviceUpdateDTO(
                name = "New Device"
            ),
            authorization = moderatorCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isNoContent)
            }
        )
    }

    @Test
    fun `updating only the device's description is allowed`() {

        val moderatorCookie = ensureCookieNotNull(getCookie(role = Role.MODERATOR))

        val deviceId = createDevice(
            input = DeviceInputDTO(
                name = "Test Device",
                description = "Test Description",
                streamURL = VALID_STREAM_URL
            )
        )?.id

        mockMvc.request<DeviceUpdateDTO, ProblemDetail>(
            method = HTTPMethod.PUT,
            uri =  "/devices/$deviceId",
            body = DeviceUpdateDTO(
                description = "New Description"
            ),
            authorization = moderatorCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isNoContent)
            }
        )
    }

    @Test
    fun `updating only the device's stream is allowed`() {

        val moderatorCookie = ensureCookieNotNull(getCookie(role = Role.MODERATOR))

        val deviceId = createDevice(
            input = DeviceInputDTO(
                name = "Test Device",
                description = "Test Description",
                streamURL = VALID_STREAM_URL
            )
        )?.id

        mockMvc.request<DeviceUpdateDTO, ProblemDetail>(
            method = HTTPMethod.PUT,
            uri =  "/devices/$deviceId",
            body = DeviceUpdateDTO(
                streamURL = VALID_STREAM_URL + "b"
            ),
            authorization = moderatorCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isNoContent)
            }
        )
    }

    @Test
    fun `adding a device, ensure it exists, update it then delete it, then ensure it does not exist`() {
        val ADMINCookie = ensureCookieNotNull(getCookie(role = Role.ADMIN))

        val createdDeviceId = createDevice(
            input = DeviceInputDTO(
                name = "Test Device",
                description = "Test Description",
                streamURL = VALID_STREAM_URL
            )
        )

        mockMvc.request<NoBody, DeviceSimpleOutputDTO>(
            method = HTTPMethod.GET,
            uri =  "/devices/${createdDeviceId?.id}",
            authorization = ADMINCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(createdDeviceId?.id))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test Device"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Test Description"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.streamURL").value(VALID_STREAM_URL))
            }
        )

        mockMvc.request<DeviceInputDTO, NoBody>(
            method = HTTPMethod.PUT,
            uri =  "/devices/${createdDeviceId?.id}",
            body = DeviceInputDTO(
                name = "Test Device Updated",
                description = "Test Description Updated",
                streamURL = VALID_STREAM_URL + "b"
            ),
            authorization = ADMINCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isNoContent)
            }
        )

        mockMvc.request<NoBody, DeviceSimpleOutputDTO>(
            method = HTTPMethod.GET,
            uri =  "/devices/${createdDeviceId?.id}",
            authorization = ADMINCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(createdDeviceId?.id))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test Device Updated"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Test Description Updated"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.streamURL").value(VALID_STREAM_URL + "b"))
            }
        )

        mockMvc.request<NoBody, NoBody>(
            method = HTTPMethod.DELETE,
            uri =  "/devices?deviceIDs=${createdDeviceId?.id}",
            authorization = ADMINCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isNoContent)
            }
        )

        mockMvc.request<NoBody, ProblemDetail>(
            method = HTTPMethod.DELETE,
            uri =  "/devices?deviceIDs=${createdDeviceId?.id}",
            authorization = ADMINCookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isNotFound)
            }
        )
    }

    @Test
    fun `Adding devices to a group then deleting a device added`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = Role.ADMIN))

        val id1 = createDevice(
            DeviceInputDTO(
                name = "Test",
                description = "Test",
                streamURL = VALID_STREAM_URL
            )
        )?.id

        val id2 = createDevice(
            DeviceInputDTO(
                name = "Test",
                description = "Test",
                streamURL = VALID_STREAM_URL
            )
        )?.id

        mockMvc.request<DevicesGroupCreateDTO, IDOutput>(
            method = HTTPMethod.POST,
            uri =  "/groups?devices=$id1,$id2",
            body = DevicesGroupCreateDTO(
                name = "Test",
                description = "Test"
            ),
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isCreated)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
            }
        )

        mockMvc.request<NoBody, ProblemDetail>(
            method = HTTPMethod.DELETE,
            uri =  "/devices?deviceIDs=$id2",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isNoContent)
            }
        )
    }

    @Test
    fun `Get devices without search`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = Role.ADMIN))

        createDevice(
            DeviceInputDTO(
                name = "Indoor Camera",
                description = "Indoor Description",
                streamURL = VALID_STREAM_URL
            )
        )?.id

        createDevice(
            DeviceInputDTO(
                name = "Outdoor Camera",
                description = "Outdoor Description",
                streamURL = VALID_STREAM_URL
            )
        )?.id

        createDevice(
            DeviceInputDTO(
                name = "Security Camera",
                description = "Security Description",
                streamURL = VALID_STREAM_URL
            )
        )?.id

        mockMvc.request<Unit, PageDTO<DeviceOutputDTO>>(
            method = HTTPMethod.GET,
            uri =  "/devices",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].name").value("Indoor Camera"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.items[1].name").value("Outdoor Camera"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.items[2].name").value("Security Camera"))
            }
        )
    }

    @Test
    fun `Get devices filtered by search`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = Role.ADMIN))

        createDevice(
            DeviceInputDTO(
                name = "Indoor Camera",
                description = "Indoor Description",
                streamURL = VALID_STREAM_URL
            )
        )?.id

        createDevice(
            DeviceInputDTO(
                name = "Outdoor Camera",
                description = "Outdoor Description",
                streamURL = VALID_STREAM_URL
            )
        )?.id

        createDevice(
            DeviceInputDTO(
                name = "Security Camera",
                description = "Security Description",
                streamURL = VALID_STREAM_URL
            )
        )?.id

        mockMvc.request<Unit, PageDTO<DeviceOutputDTO>>(
            method = HTTPMethod.GET,
            uri =  "/devices?search=Camera",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].name").value("Indoor Camera"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.items[1].name").value("Outdoor Camera"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.items[2].name").value("Security Camera"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.items[3]").doesNotExist())
            }
        )

        mockMvc.request<Unit, PageDTO<DeviceOutputDTO>>(
            method = HTTPMethod.GET,
            uri =  "/devices?search=Indoor",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].name").value("Indoor Camera"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.items[1]").doesNotExist())
            }
        )

        mockMvc.request<Unit, PageDTO<DeviceOutputDTO>>(
            method = HTTPMethod.GET,
            uri =  "/devices?search=Outdoor",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].name").value("Outdoor Camera"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.items[1]").doesNotExist())
            }
        )
    }

    @Test
    fun `Get devices filtered by half name search`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = Role.ADMIN))

        createDevice(
            DeviceInputDTO(
                name = "Indoor Camera",
                description = "Indoor Description",
                streamURL = VALID_STREAM_URL
            )
        )?.id

        createDevice(
            DeviceInputDTO(
                name = "Inner Camera",
                description = "Outdoor Description",
                streamURL = VALID_STREAM_URL
            )
        )?.id

        mockMvc.request<Unit, PageDTO<DeviceOutputDTO>>(
            method = HTTPMethod.GET,
            uri =  "/devices?search=In",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].name").value("Indoor Camera"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.items[1].name").value("Inner Camera"))
            }
        )
    }

    @Test
    fun `Get devices filtered by empty search`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = Role.ADMIN))

        createDevice(
            DeviceInputDTO(
                name = "Indoor Camera",
                description = "Indoor Description",
                streamURL = VALID_STREAM_URL
            )
        )?.id

        createDevice(
            DeviceInputDTO(
                name = "Inner Camera",
                description = "Outdoor Description",
                streamURL = VALID_STREAM_URL
            )
        )?.id

        mockMvc.request<Unit, PageDTO<DeviceOutputDTO>>(
            method = HTTPMethod.GET,
            uri =  "/devices?search=",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].name").value("Indoor Camera"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.items[1].name").value("Inner Camera"))
            }
        )
    }

    @Test
    fun `Get devices filtered by empty search paginated`() {
        val cookie = ensureCookieNotNull(cookie = getCookie(role = Role.ADMIN))

        createDevice(
            DeviceInputDTO(
                name = "Indoor Camera",
                description = "Indoor Description",
                streamURL = VALID_STREAM_URL
            )
        )?.id

        createDevice(
            DeviceInputDTO(
                name = "Inner Camera",
                description = "Outdoor Description",
                streamURL = VALID_STREAM_URL
            )
        )?.id

        createDevice(
            DeviceInputDTO(
                name = "Security Camera",
                description = "Outdoor Description",
                streamURL = VALID_STREAM_URL
            )
        )?.id

        mockMvc.request<Unit, PageDTO<DeviceOutputDTO>>(
            method = HTTPMethod.GET,
            uri =  "/devices?search=In&page=0&pageSize=1",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(2))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.isLast").value(false))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].name").value("Indoor Camera"))
            }
        )

        mockMvc.request<Unit, PageDTO<DeviceOutputDTO>>(
            method = HTTPMethod.GET,
            uri =  "/devices?search=In&page=1&pageSize=1",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(2))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.isLast").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].name").value("Inner Camera"))
            }
        )

        mockMvc.request<Unit, PageDTO<DeviceOutputDTO>>(
            method = HTTPMethod.GET,
            uri =  "/devices?search=In&page=2&pageSize=1",
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(2))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.isLast").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.items[0]").doesNotExist())
            }
        )
    }

    private fun createDevice(input: DeviceInputDTO): IDOutput? {
        val userRole = Role.MODERATOR
        val cookie = ensureCookieNotNull(getCookie(role = userRole))

        return mockMvc.request<DeviceInputDTO, IDOutput>(
            method = HTTPMethod.POST,
            uri =  "/devices",
            body = input,
            authorization = cookie,
            mapper = mapper,
            assertions = {
                andExpect(MockMvcResultMatchers.status().isCreated)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
            }
        )
    }

    private fun getCookie(role: Role = Role.USER, emailCounter: Int = counter++): Cookie? {
        val inputLogin = createTestUser(userService, role, emailCounter)
        val loginJson = mapper.writeValueAsString(inputLogin)

        val loginResult = mockMvc.perform(
            MockMvcRequestBuilders.post( "/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson)
        ).andExpect(MockMvcResultMatchers.status().isOk)

        return loginResult
            .andReturn()
            .response
            .getCookie(Constants.User.AUTH_COOKIE_NAME)
    }
}
