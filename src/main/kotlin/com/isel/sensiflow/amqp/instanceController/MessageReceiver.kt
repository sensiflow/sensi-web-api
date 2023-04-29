package com.isel.sensiflow.amqp.instanceController

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.isel.sensiflow.amqp.InstanceResponseMessage
import com.isel.sensiflow.services.DeviceService
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class MessageReceiver(
    val deviceService: DeviceService
) {

    companion object {
        private val mapper = jacksonObjectMapper()
        private val logger = LoggerFactory.getLogger(MessageReceiver::class.java)
    }

    /**
     * Listener that receives messages from the queue and acts accordingly.
     */
    @RabbitListener(queues = ["\${rabbit.mq.ack_queue}"])
    fun receiveMessageFromAckQueue(message: Message) {
        val instanceResponseMessage = mapper.readValue(String(message.body), InstanceResponseMessage::class.java)
        logger.info("Received message from ack_queue: $instanceResponseMessage")

        val newState = instanceResponseMessage.newState
            .takeIf { (instanceResponseMessage.code % 1000) != 4 }

        deviceService.completeUpdateState(instanceResponseMessage.device_id, newState)
    }
}
