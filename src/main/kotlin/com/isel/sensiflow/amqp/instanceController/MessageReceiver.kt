package com.isel.sensiflow.amqp.instanceController

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.isel.sensiflow.amqp.DeleteDeviceMessage
import com.isel.sensiflow.amqp.DeviceStateResponseMessage
import com.isel.sensiflow.services.DeviceService
import com.isel.sensiflow.services.ServiceException
import com.rabbitmq.client.Channel
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
    fun receiveMessageFromAckDeviceStateQueue(message: Message, channel: Channel) {
        try {
            val instanceResponseMessage = mapper.readValue(String(message.body), DeviceStateResponseMessage::class.java)
            logger.info("Received message from ack_device_state_queue: $instanceResponseMessage")

            val newState = instanceResponseMessage.newState
                .takeIf { (instanceResponseMessage.code % 1000) != 4 }

            deviceService.completeUpdateState(instanceResponseMessage.device_id, newState)
        } catch (e: ServiceException) {
            logger.warn("Error while processing message from ack_device_state_queue: ${e.message}")
            channel.basicAck(message.messageProperties.deliveryTag, false)
        }//TODO: dead letter, for when db is down , add
    }
//TODO: change to just 1 queue
    /**
     * Listener that receives messages from the queue and acts accordingly.
     */
    @RabbitListener(queues = ["\${rabbit.mq.ack_device_delete_queue}"])
    fun receiveMessageFromAckDeviceDeleteQueue(message: Message) {//TODO: ver situaçao de erro
        val deleteDeviceResponseMessage = mapper.readValue(String(message.body), DeleteDeviceMessage::class.java)
        logger.info("Received message from ack_device_delete_queue: $deleteDeviceResponseMessage")

        deviceService.completeDeviceDeletion(
            deviceID = deleteDeviceResponseMessage.device_id
        )
    }
}
