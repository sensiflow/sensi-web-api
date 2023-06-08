package com.isel.sensiflow

import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EnableRabbit
@EnableJpaRepositories
@SpringBootApplication
class SensiflowApplication

fun main(args: Array<String>) {
    runApplication<SensiflowApplication>(*args)
}
