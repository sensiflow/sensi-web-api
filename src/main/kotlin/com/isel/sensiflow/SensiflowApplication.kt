package com.isel.sensiflow

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication()
class SensiflowApplication {
    @Value("\${spring.datasource.url}")
    private val dbUrl: String? = null

}

fun main(args: Array<String>) {
    runApplication<SensiflowApplication>(*args)
}
