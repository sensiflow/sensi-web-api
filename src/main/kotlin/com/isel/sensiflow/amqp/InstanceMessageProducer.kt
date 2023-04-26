package com.isel.sensiflow.amqp

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class InstanceMessageProducer(
    val rabbitTemplate: RabbitTemplate
) {

    companion object {
        const val QUEUE_NAME = "devices"
        private val mapper = jacksonObjectMapper()
    }

    /**
     * Send a [InstanceMessage] to the queue.
     */
    fun sendMessage(message: InstanceMessage) {
        rabbitTemplate.convertAndSend(QUEUE_NAME, mapper.writeValueAsString(message))
    }

    /**
     * Receive a [InstanceMessage] from the queue.
     */
    fun receiveMessage(): InstanceMessage? {
        TODO()
    }
}
