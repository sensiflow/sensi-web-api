package com.isel.sensiflow.amqp.instanceController

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.isel.sensiflow.amqp.DeviceStateResponseMessage
import com.isel.sensiflow.amqp.ProcessingAction
import com.isel.sensiflow.amqp.SchedulerNotificationResponseMessage
import com.isel.sensiflow.amqp.isError
import com.isel.sensiflow.amqp.isNotFound
import com.isel.sensiflow.amqp.isSuccessful
import com.isel.sensiflow.model.entities.DeviceProcessingState
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

            if(instanceResponseMessage.action == ProcessingAction.REMOVE.name){
                deviceService.completeDeviceDeletion(instanceResponseMessage.device_id)
                return
            }

            val newState = when{
                instanceResponseMessage.isSuccessful() -> instanceResponseMessage.newState
                instanceResponseMessage.isNotFound() -> DeviceProcessingState.INACTIVE
                instanceResponseMessage.isError() -> null
                else -> throw InternalError("Invalid response code: ${instanceResponseMessage.code}")
            }

            deviceService.completeUpdateState(instanceResponseMessage.device_id, newState)
        } catch (e: ServiceException) {
            logger.warn("Error while processing message from ack_device_state_queue: ${e.message}")
        }
    }

    /**
     * Listener that receives messages from the queue and acts accordingly.
     */
    @RabbitListener(queues = ["\${rabbit.mq.instance_scheduler_notification}"])
    fun receiveMessageFromAckSchedulerNotificationQueue(message: Message, channel: Channel) {
        try {
            val schedulerNotificationMessage = mapper.readValue(
                String(message.body),
                SchedulerNotificationResponseMessage::class.java
            )
            logger.info("Received message from instance_scheduler_notification: $schedulerNotificationMessage")

            schedulerNotificationMessage.device_ids.forEach { deviceId ->
                deviceService.completeUpdateState(deviceId, DeviceProcessingState.INACTIVE)
            }
            logger.info("Updated devices state with ids ${schedulerNotificationMessage.device_ids} to INACTIVE")

        } catch (e: ServiceException) {
            logger.warn("Error while processing message from instance_scheduler_notification: ${e.message}")
        }
    }
}
