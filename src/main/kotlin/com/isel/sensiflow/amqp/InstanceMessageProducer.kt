package com.isel.sensiflow.amqp

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class InstanceMessageProducer(
    val rabbitTemplate: RabbitTemplate
) {

    @Value("\${spring.rabbitmq.template.default-receive-queue}")
    private lateinit var queueName: String

    companion object {
        private val mapper = jacksonObjectMapper()
    }

    /**
     * Send a [InstanceMessage] to the queue.
     */
    fun sendMessage(message: InstanceMessage) {
        rabbitTemplate.convertAndSend(queueName, mapper.writeValueAsString(message))
    }
}
