package com.isel.sensiflow.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.isel.sensiflow.http.controller.RequestPaths
import com.isel.sensiflow.http.entities.input.UserRegisterInput
import org.junit.Before
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.Rollback
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(org.springframework.test.context.junit4.SpringRunner::class)
@AutoConfigureMockMvc
class DevicesGroupControllerTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    companion object {
        val mapper = jacksonObjectMapper()
    }

    @Test
    fun `test get all devices groups`() {

    }

    private fun createUser() {
        val user = UserRegisterInput(
            email = "test@email.com",
            firstName = "Test",
            lastName = "Test",
            password = "Password1_"
        )
        val json = mapper.writeValueAsString(user)

        mockMvc.perform(
            post(RequestPaths.Users.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isCreated)
    }
}

fun WebTestClient.RequestHeadersSpec<*>.setContentTypeJson(): WebTestClient.RequestHeadersSpec<*> =
    header("Content-Type", "application/json")