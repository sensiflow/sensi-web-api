package com.isel.sensiflow.amqp.instanceController

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.isel.sensiflow.amqp.ProcessingAction
import com.isel.sensiflow.amqp.message.output.InstanceMessage
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
    private lateinit var controllerQueueRoutingKey: String
    private val generalRoutingQueue = "im.general"

    /**
     * Send a [InstanceMessage] to the queue.
     */
    fun sendMessage(message: InstanceMessage) {

        val (routingKey, msg) = if (message.action == ProcessingAction.START)
            Pair(
                controllerQueueRoutingKey,
                message
            )
        else {
            val newAction = if (message.action == ProcessingAction.RESUME) ProcessingAction.START else message.action
            Pair(
                generalRoutingQueue,
                InstanceMessage(
                    device_id = message.device_id,
                    device_stream_url = message.device_stream_url,
                    action = newAction
                )
            )
        }

        rabbitTemplate.convertAndSend("${controllerQueueRoutingKey}_exchange", routingKey, mapper.writeValueAsString(msg))
    }
}
