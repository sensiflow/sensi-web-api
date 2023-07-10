package com.isel.sensiflow.amqp.instanceController

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.isel.sensiflow.amqp.message.MessageHandler
import com.isel.sensiflow.amqp.message.input.InstanceCtlAcknowledgeMessage
import com.isel.sensiflow.amqp.message.input.SchedulerNotificationResponseMessage
import com.isel.sensiflow.model.entities.DeviceProcessingState
import com.isel.sensiflow.services.ServiceException
import com.isel.sensiflow.services.beans.DeviceProcessingStateService
import com.rabbitmq.client.Channel
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class MessageReceiver(
    val messageHandler: MessageHandler,
    val completionService: DeviceProcessingStateService
) {

    companion object {
        private val mapper = jacksonObjectMapper()
        private val logger = LoggerFactory.getLogger(MessageReceiver::class.java)
    }

    /**
     * Listener that receives messages from the queue and acts accordingly.
     */
    @RabbitListener(queues = ["\${rabbit.mq.ack_device_state_queue}"])
    fun receiveMessageFromControlAcknowledgeQueue(message: Message, channel: Channel) {
        try {
            val instanceResponseMessage = mapper.readValue(String(message.body), InstanceCtlAcknowledgeMessage::class.java)
            logger.info("Received message from ack_device_state_queue: $instanceResponseMessage")
            messageHandler.handle(instanceResponseMessage)
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
                completionService.completeUpdateState(deviceId, DeviceProcessingState.INACTIVE, true)
            }
            logger.info("Updated devices state with ids ${schedulerNotificationMessage.device_ids} to INACTIVE")
        } catch (e: ServiceException) {
            logger.warn("Error while processing message from instance_scheduler_notification: ${e.message}")
        }
    }
}
