package com.isel.sensiflow.services

import com.isel.sensiflow.Constants
import com.isel.sensiflow.amqp.Action
import com.isel.sensiflow.amqp.InstanceMessage
import com.isel.sensiflow.amqp.action
import com.isel.sensiflow.amqp.instanceController.MessageSender
import com.isel.sensiflow.model.dao.Device
import com.isel.sensiflow.model.dao.DeviceProcessingState
import com.isel.sensiflow.model.repository.DeviceGroupRepository
import com.isel.sensiflow.model.repository.DeviceRepository
import com.isel.sensiflow.model.repository.MetricRepository
import com.isel.sensiflow.model.repository.ProcessedStreamRepository
import com.isel.sensiflow.model.repository.UserRepository
import com.isel.sensiflow.services.dto.PageableDTO
import com.isel.sensiflow.services.dto.input.DeviceInputDTO
import com.isel.sensiflow.services.dto.input.DeviceUpdateDTO
import com.isel.sensiflow.services.dto.input.fieldsAreEmpty
import com.isel.sensiflow.services.dto.input.isTheSameAs
import com.isel.sensiflow.services.dto.output.DeviceOutputDTO
import com.isel.sensiflow.services.dto.output.DeviceProcessingStateOutput
import com.isel.sensiflow.services.dto.output.MetricOutputDTO
import com.isel.sensiflow.services.dto.output.PageDTO
import com.isel.sensiflow.services.dto.output.toDeviceOutputDTO
import com.isel.sensiflow.services.dto.output.toDeviceProcessingStateOutput
import com.isel.sensiflow.services.dto.output.toMetricOutputDTO
import com.isel.sensiflow.services.dto.output.toPageDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class DeviceService(
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository,
    private val metricRepository: MetricRepository,
    private val processedStreamRepository: ProcessedStreamRepository,
    private val instanceControllerMessageSender: MessageSender,
    private val deviceGroupRepository: DeviceGroupRepository,
) {

    /**
     * Creates a new device.
     * @param deviceInput The input data for the device.
     * @param userID The id of the user that owns the device.
     * @return The created device.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun createDevice(deviceInput: DeviceInputDTO, userID: Int): DeviceOutputDTO {
        val user = userRepository.findById(userID)
            .orElseThrow { UserNotFoundException(userID) }
        val newDevice = Device(
            name = deviceInput.name,
            streamURL = deviceInput.streamURL,
            description = deviceInput.description ?: "",
            user = user
        )

        return deviceRepository
            .save(newDevice)
            .toDeviceOutputDTO(expanded = false)
    }

    /**
     * Gets a device by its id.
     * @param deviceID The id of the device.
     * @param expanded If the device should contain embedded entities.
     * @return The requested device.
     * @throws DeviceNotFoundException If the device does not exist.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    fun getDeviceById(deviceID: Int, expanded: Boolean): DeviceOutputDTO {
        return deviceRepository.findById(deviceID)
            .orElseThrow { DeviceNotFoundException(deviceID) }
            .toDeviceOutputDTO(expanded)
    }

    /**
     * Gets all devices.
     * @param pageableDTO The pagination information.
     * @return A [PageDTO] of devices.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    fun getAllDevices(pageableDTO: PageableDTO, expanded: Boolean): PageDTO<DeviceOutputDTO> {
        val pageable: Pageable = PageRequest.of(pageableDTO.page, pageableDTO.size)
        return deviceRepository
            .findAll(pageable)
            .map { deviceDao -> deviceDao.toDeviceOutputDTO(expanded = expanded) }
            .toPageDTO()
    }

    /**
     * Updates a device.
     *
     * All provided fields are overwritten, except if in the input the field is null.
     *
     * @param deviceID The id of the device.
     * @param deviceUpdateInput The input data for the device to update.
     * @return The updated [Device].
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun updateDevice(deviceID: Int, deviceUpdateInput: DeviceUpdateDTO) {

        val storedDevice = deviceRepository.findById(deviceID)
            .orElseThrow { DeviceNotFoundException(deviceID) }

        if (deviceUpdateInput.fieldsAreEmpty() || storedDevice.isTheSameAs(deviceUpdateInput))
            return

        val updatedDevice = Device(
            id = storedDevice.id,
            name = deviceUpdateInput.name ?: storedDevice.name,
            streamURL = deviceUpdateInput.streamURL ?: storedDevice.streamURL,
            description = deviceUpdateInput.description ?: storedDevice.description,
            user = storedDevice.user
        )

        deviceRepository.save(updatedDevice)
    }

    /**
     * Deletes a device.
     * @param deviceID The id of the device.
     * @throws DeviceNotFoundException If the device does not exist.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun deleteDevice(deviceID: Int) {
        val device = deviceRepository.findById(deviceID)
            .orElseThrow { DeviceNotFoundException(deviceID) }

        device.deviceGroups.forEach { deviceGroup ->
            deviceGroup.devices.remove(device)
            deviceGroupRepository.save(deviceGroup)
        }

        processedStreamRepository.deleteAllByDevice(device)

        metricRepository.deleteAllByDevice(device)

        val queueMessage = InstanceMessage(
            action = Action.REMOVE,
            device_id = deviceID,
            device_stream_url = null
        )

        deviceRepository.flagForDeletion(deviceID)

        instanceControllerMessageSender.sendMessage(queueMessage)
    }

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
            user = storedDevice.user,
            processingState = storedDevice.processingState,
            pendingUpdate = true
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
     * @param newProcessingState The new state of the device.
     */
    fun completeUpdateState(deviceID: Int, newProcessingState: DeviceProcessingState?) {
        val storedDevice = deviceRepository.findById(deviceID)
            .orElseThrow { DeviceNotFoundException(deviceID) }

        if (!storedDevice.pendingUpdate)
            throw ServiceInternalException("The device is not pending an update.")

        val deviceWithUpdatedState = Device(
            id = storedDevice.id,
            name = storedDevice.name,
            streamURL = storedDevice.streamURL,
            description = storedDevice.description,
            user = storedDevice.user,
            processingState = newProcessingState ?: storedDevice.processingState,
            pendingUpdate = false
        )

        deviceRepository.save(deviceWithUpdatedState)
    }

    /**
     *  Completes the deletion of a device.
     *  @param deviceID The id of the device.
     */
    fun completeDeviceDeletion(deviceID: Int) {
        val storedDevice = deviceRepository.findById(deviceID)
            .orElseThrow { DeviceNotFoundException(deviceID) }

        if (!storedDevice.scheduledForDeletion)
            throw ServiceInternalException("The device is not scheduled for deletion.")

        deviceRepository.delete(storedDevice)
    }

    /**
     * Gets the stats of a device.
     * @param pageableDTO The pagination information.
     * @param deviceId The id of the device.
     * @throws DeviceNotFoundException If the device does not exist.
     * @return A [PageDTO] of [MetricOutputDTO].
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    fun getDeviceStats(pageableDTO: PageableDTO, deviceId: Int): PageDTO<MetricOutputDTO> {
        val pageable: Pageable = PageRequest.of(pageableDTO.page, pageableDTO.size)

        val storedDevice = deviceRepository.findById(deviceId)
            .orElseThrow { DeviceNotFoundException(deviceId) }

        return metricRepository
            .findAllByDeviceId(storedDevice.id, pageable)
            .map { metric -> metric.toMetricOutputDTO() }
            .toPageDTO()
    }

    /**
     * TODO: Comment
     */
    fun getPeopleCountFlow(deviceID: ID): Flow<Int> {
        return flow<Int> {
            while (true) {
                val storedDevice = deviceRepository.findById(deviceID)
                    .orElseThrow { DeviceNotFoundException(deviceID) }

                if (storedDevice.processingState != DeviceProcessingState.ACTIVE)
                    break

                val latestMetric = metricRepository.findByMaxStartTime(deviceID)

                if (latestMetric.isPresent) emit(latestMetric.get().peopleCount)

                delay(Constants.Device.PEOPLE_COUNT_RETRIEVAL_DELAY)
            }
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Gets the processing state of a device.
     */
    fun getDeviceStateFlow(Id: ID): Flow<DeviceProcessingStateOutput> =
        flow {
            while (true) {
                val device = deviceRepository.findById(Id)
                    .orElseThrow { DeviceNotFoundException(Id) }

                if (!device.pendingUpdate) {
                    emit(device.processingState.toDeviceProcessingStateOutput())
                    break
                }

                emit(DeviceProcessingStateOutput.PENDING)
                delay(Constants.Device.DEVICE_PROCESSING_STATE_RETRIEVAL_DELAY)
            }
        }.flowOn(Dispatchers.IO)
}
