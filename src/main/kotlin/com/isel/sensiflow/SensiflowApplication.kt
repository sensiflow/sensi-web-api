package com.isel.sensiflow

import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@EnableRabbit
@SpringBootApplication
class SensiflowApplication

fun main(args: Array<String>) {
    runApplication<SensiflowApplication>(*args)
}
