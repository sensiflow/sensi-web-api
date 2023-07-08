package com.isel.sensiflow.services.beans

import com.isel.sensiflow.Constants
import com.isel.sensiflow.amqp.action
import com.isel.sensiflow.amqp.instanceController.MessageSender
import com.isel.sensiflow.amqp.message.output.InstanceMessage
import com.isel.sensiflow.model.entities.Device
import com.isel.sensiflow.model.entities.DeviceProcessingState
import com.isel.sensiflow.model.repository.DeviceRepository
import com.isel.sensiflow.services.DeviceAlreadyUpdatingException
import com.isel.sensiflow.services.DeviceNotFoundException
import com.isel.sensiflow.services.ID
import com.isel.sensiflow.services.InvalidProcessingStateException
import com.isel.sensiflow.services.InvalidProcessingStateTransitionException
import com.isel.sensiflow.services.ServiceInternalException
import com.isel.sensiflow.services.dto.output.DeviceProcessingStateOutput
import com.isel.sensiflow.services.dto.output.toDeviceProcessingStateOutput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class DeviceProcessingStateService(
    val deviceRepository: DeviceRepository,
    val instanceControllerMessageSender: MessageSender
) {

    /**
     * Changes the processing state of a device.
     * @param deviceID The id of the device.
     * @param newStateInput The new state of the device.
     * @throws DeviceNotFoundException If the device does not exist.
     * @throws InvalidProcessingStateException If the new state is not valid.
     * @throws InvalidProcessingStateTransitionException If the new state is not a valid transition from the current state.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun startUpdateProcessingState(deviceID: Int, newStateInput: String) {
        val newProcessingState: DeviceProcessingState = DeviceProcessingState.fromString(newStateInput)
            ?: throw InvalidProcessingStateException(newStateInput)

        val storedDevice = deviceRepository.findById(deviceID)
            .orElseThrow { DeviceNotFoundException(deviceID) }

        if (storedDevice.pendingUpdate)
            throw DeviceAlreadyUpdatingException(deviceID)

        if (storedDevice.processingState == newProcessingState)
            return

        if (!storedDevice.processingState.isValidTransition(newProcessingState))
            throw InvalidProcessingStateTransitionException(storedDevice.processingState, newProcessingState)

        val deviceWithUpdatedState = Device(
            id = storedDevice.id,
            name = storedDevice.name,
            streamURL = storedDevice.streamURL,
            description = storedDevice.description,
            processingState = storedDevice.processingState,
            pendingUpdate = true,
            processedStreamURL = storedDevice.processedStreamURL
        )

        deviceRepository.save(deviceWithUpdatedState)

        val queueMessage = InstanceMessage(
            action = newProcessingState.action,
            device_id = deviceID,
            device_stream_url = storedDevice.streamURL
        )

        instanceControllerMessageSender.sendMessage(queueMessage)
    }

    /**
     * Forces an update on the processing state of a device.
     * @param deviceID The id of the device.
     * @param newProcessingState The new state of the device (null if no update).
     */
    fun completeUpdateState(deviceID: Int, newProcessingState: DeviceProcessingState?, forceUpdate : Boolean = false) {
        val storedDevice = deviceRepository.findById(deviceID)
            .orElseThrow { DeviceNotFoundException(deviceID) }

        if (!forceUpdate && !storedDevice.pendingUpdate)
            throw ServiceInternalException("The device is not pending an update.")

        val deviceWithUpdatedState = Device(
            id = storedDevice.id,
            name = storedDevice.name,
            streamURL = storedDevice.streamURL,
            description = storedDevice.description,
            processingState = newProcessingState ?: storedDevice.processingState,
            pendingUpdate = false,
            processedStreamURL = storedDevice.processedStreamURL
        )

        deviceRepository.save(deviceWithUpdatedState)
    }

    /**
     * Gets the processing state of a device.
     *
     * This method will continuously retrieve the latest processing
     * state of a device until the device is no longer pending an update.
     */
    fun getDeviceStateFlow(id: ID): Flow<DeviceProcessingStateOutput> =
        flow {
            while (true) {
                val device = deviceRepository.findById(id)
                    .orElseThrow { DeviceNotFoundException(id) }

                if (!device.pendingUpdate) {
                    emit(device.processingState.toDeviceProcessingStateOutput())
                    break
                }

                emit(DeviceProcessingStateOutput.PENDING)
                delay(Constants.Device.DEVICE_PROCESSING_STATE_RETRIEVAL_DELAY)
            }
        }.flowOn(Dispatchers.IO)
}
