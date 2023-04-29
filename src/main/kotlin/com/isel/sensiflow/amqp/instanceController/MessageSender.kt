package com.isel.sensiflow.amqp.instanceController

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.isel.sensiflow.amqp.InstanceMessage
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MessageSender(
    val rabbitTemplate: RabbitTemplate
) {

    companion object {
        private val mapper = jacksonObjectMapper()
    }

    @Value("\${rabbit.mq.ctl_queue}")
    private lateinit var controllerQueue: String

    /**
     * Send a [InstanceMessage] to the queue.
     */
    fun sendMessage(message: InstanceMessage) {
        rabbitTemplate.convertAndSend(controllerQueue, mapper.writeValueAsString(message))
    }
}
