package com.isel.sensiflow

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SensiflowApplication

fun main(args: Array<String>) {
    runApplication<SensiflowApplication>(*args)
}
