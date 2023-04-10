package com.isel.sensiflow.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.isel.sensiflow.Constants
import com.isel.sensiflow.http.entities.output.IDOutput
import com.isel.sensiflow.model.dao.Metric
import com.isel.sensiflow.model.dao.MetricID
import com.isel.sensiflow.model.repository.DeviceRepository
import com.isel.sensiflow.model.repository.MetricRepository
import com.isel.sensiflow.services.Role
import com.isel.sensiflow.services.UserService
import com.isel.sensiflow.services.dto.input.DeviceInputDTO
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
@AutoConfigureMockMvc
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
        val mapper = jacksonObjectMapper()
    }

    @Test
    fun `get device stats successfully`() {
        val cookie = ensureCookieNotNull(cookie = getCookie())

        val responseDevice1 = createDevice(
            cookie,
            DeviceInputDTO(
                name = "Test",
                description = "Test",
                streamURL = "Test.url"
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
                deviceID = device
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
                deviceID = device
            )
        )

        mockMvc.request<Unit, PageDTO<MetricOutputDTO>>(
            method = HTTPMethod.GET,
            uri = "/devices/$id1/stats?page=0&size=10",
            authorization = cookie,
            mapper = DevicesGroupControllerTests.mapper,
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
        val cookie = ensureCookieNotNull(cookie = getCookie())

        mockMvc.request<Unit, ProblemDetail>(
            method = HTTPMethod.GET,
            uri = "/devices/-1/stats?page=0&size=10",
            authorization = cookie,
            mapper = DevicesGroupControllerTests.mapper,
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
            mapper = mapper,
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
