package com.isel.sensiflow.amqp.message

import com.isel.sensiflow.amqp.ProcessingAction
import com.isel.sensiflow.amqp.message.input.InstanceCtlAcknowledgeMessage
import com.isel.sensiflow.amqp.message.input.isError
import com.isel.sensiflow.amqp.message.input.isNotFound
import com.isel.sensiflow.amqp.message.input.isSuccessful
import com.isel.sensiflow.model.entities.DeviceProcessingState
import com.isel.sensiflow.services.beans.DeviceProcessingStateService
import com.isel.sensiflow.services.beans.DeviceService
import org.springframework.stereotype.Component

typealias ActionHandler = (InstanceCtlAcknowledgeMessage) -> Unit

/**
 * Message handler for acknowledge messages.
 *
 * Acts as a dispatcher for the different actions that can be performed on a device's processing.
 *
 * @property processingStateService the service that handles the completion of the device processing state.
 * @property deviceService the service that handles the completion of the device deletion.
 */
@Component
class CompletionMessageHandler(
    private val processingStateService: DeviceProcessingStateService,
    private val deviceService: DeviceService
) : MessageHandler {

    /**
     * Dispatcher for the different actions that can be performed when receiving an acknowledgment message.
     */
    private val dispatcher = mapOf<ProcessingAction, ActionHandler>(
        ProcessingAction.REMOVE to { deviceService.completeDeviceDeletion(it.device_id) },
        ProcessingAction.START to ::deviceStateUpdate,
        ProcessingAction.STOP to ::deviceStateUpdate,
        ProcessingAction.PAUSE to ::deviceStateUpdate
    )

    /**
     * Handles the received acknowledge message.
     */
    override fun handle(message: InstanceCtlAcknowledgeMessage) {
        val processingAction = ProcessingAction.fromString(message.action)
        val handler = dispatcher[processingAction]
        handler?.invoke(message)
    }

    private fun deviceStateUpdate(acknowledgeMessage: InstanceCtlAcknowledgeMessage) {
        val newState = when {
            acknowledgeMessage.isSuccessful() -> acknowledgeMessage.newState
            acknowledgeMessage.isNotFound() -> DeviceProcessingState.INACTIVE
            acknowledgeMessage.isError() -> null
            else -> throw InternalError("Invalid response code: ${acknowledgeMessage.code}")
        }

        processingStateService.completeUpdateState(acknowledgeMessage.device_id, newState)
    }
}
