package com.isel.sensiflow.amqp.instanceController

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.isel.sensiflow.amqp.DeleteDeviceMessage
import com.isel.sensiflow.amqp.DeviceStateResponseMessage
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
    @RabbitListener(queues = ["\${rabbit.mq.ack_device_state_queue}"])
    fun receiveMessageFromAckDeviceStateQueue(message: Message) {
        val instanceResponseMessage = mapper.readValue(String(message.body), DeviceStateResponseMessage::class.java)
        logger.info("Received message from ack_device_state_queue: $instanceResponseMessage")

        val newState = instanceResponseMessage.newState
            .takeIf { (instanceResponseMessage.code % 1000) != 4 }

        deviceService.completeUpdateState(instanceResponseMessage.device_id, newState)
    }

    /**
     * Listener that receives messages from the queue and acts accordingly.
     */
    @RabbitListener(queues = ["\${rabbit.mq.ack_device_delete_queue}"])
    fun receiveMessageFromAckDeviceDeleteQueue(message: Message) {
        val deleteDeviceResponseMessage = mapper.readValue(String(message.body), DeleteDeviceMessage::class.java)
        logger.info("Received message from ack_device_delete_queue: $deleteDeviceResponseMessage")

        deviceService.completeDeviceDeletion(
            deviceID = deleteDeviceResponseMessage.device_id
        )
    }
}
