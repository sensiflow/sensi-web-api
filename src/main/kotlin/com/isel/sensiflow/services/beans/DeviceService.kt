package com.isel.sensiflow.services.beans

import com.isel.sensiflow.Constants
import com.isel.sensiflow.amqp.ProcessingAction
import com.isel.sensiflow.amqp.action
import com.isel.sensiflow.amqp.instanceController.MessageSender
import com.isel.sensiflow.amqp.message.output.InstanceMessage
import com.isel.sensiflow.model.entities.Device
import com.isel.sensiflow.model.entities.DeviceProcessingState
import com.isel.sensiflow.model.repository.DeviceGroupRepository
import com.isel.sensiflow.model.repository.DeviceRepository
import com.isel.sensiflow.model.repository.MetricRepository
import com.isel.sensiflow.model.repository.requireFindAllById
import com.isel.sensiflow.services.DeviceNotFoundException
import com.isel.sensiflow.services.ID
import com.isel.sensiflow.services.ServiceInternalException
import com.isel.sensiflow.services.dto.PageableDTO
import com.isel.sensiflow.services.dto.input.DeviceInputDTO
import com.isel.sensiflow.services.dto.input.DeviceUpdateDTO
import com.isel.sensiflow.services.dto.input.fieldsAreEmpty
import com.isel.sensiflow.services.dto.input.isTheSameAs
import com.isel.sensiflow.services.dto.output.DeviceOutputDTO
import com.isel.sensiflow.services.dto.output.MetricOutputDTO
import com.isel.sensiflow.services.dto.output.PageDTO
import com.isel.sensiflow.services.dto.output.toDeviceOutputDTO
import com.isel.sensiflow.services.dto.output.toMetricOutputDTO
import com.isel.sensiflow.services.dto.output.toPageDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class DeviceService(
    private val deviceRepository: DeviceRepository,
    private val metricRepository: MetricRepository,
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
        val newDevice = Device(
            name = deviceInput.name,
            streamURL = deviceInput.streamURL,
            description = deviceInput.description ?: "",
            processedStreamURL = null
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
    fun getAllDevices(
        pageableDTO: PageableDTO,
        expanded: Boolean,
        search: String? = null
    ): PageDTO<DeviceOutputDTO> {
        val pageable: Pageable = PageRequest.of(pageableDTO.page, pageableDTO.size)

        return deviceRepository
            .getDevicesBy(search, pageable)
            .map { deviceDao -> deviceDao.toDeviceOutputDTO(expanded = expanded) }
            .toPageDTO()
    }

    private fun DeviceRepository.getDevicesBy(search: String?, pageable: Pageable): Page<Device> =
        if (search != null) {
            this.findAll(search, pageable)
        } else {
            this.findAll(pageable)
        }

    /**
     * Updates the device metadata.
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

        val stopDeviceCondition =
            deviceUpdateInput.streamURL != storedDevice.streamURL &&
                storedDevice.processingState != DeviceProcessingState.INACTIVE

        val updatedDevice = Device(
            id = storedDevice.id,
            name = deviceUpdateInput.name ?: storedDevice.name,
            streamURL = deviceUpdateInput.streamURL ?: storedDevice.streamURL,
            description = deviceUpdateInput.description ?: storedDevice.description,
            processedStreamURL = storedDevice.processedStreamURL,
            pendingUpdate = stopDeviceCondition
        )

        if (stopDeviceCondition) {
            instanceControllerMessageSender.sendMessage(
                InstanceMessage(
                    action = ProcessingAction.STOP,
                    deviceID,
                    null
                )
            )
        }

        deviceRepository.save(updatedDevice)
    }

    /**
     * Deletes devices.
     * @param deviceIDs The ids of the devices.
     * @throws DeviceNotFoundException If one of the devices does not exist.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun deleteDevices(deviceIDs: List<Int>) {
        val devicesToDelete = deviceRepository.requireFindAllById(deviceIDs)

        deviceRepository.flagForDeletion(devicesToDelete)

        val deviceGroups = devicesToDelete.flatMap { it.deviceGroups }

        deviceGroups.forEach { group ->
            group.devices.removeAll(devicesToDelete.toSet())
        }

        deviceGroupRepository.saveAll(deviceGroups)

        devicesToDelete
            .map { device ->
                InstanceMessage(
                    action = ProcessingAction.REMOVE,
                    device_id = device.id,
                    device_stream_url = null
                )
            }.forEach { instanceControllerMessageSender.sendMessage(it) }
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

        metricRepository.deleteAllByDevice(storedDevice)

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
    fun getDeviceStats(deviceId: Int, pageableDTO: PageableDTO): PageDTO<MetricOutputDTO> {
        val pageable: Pageable = PageRequest.of(pageableDTO.page, pageableDTO.size)

        val storedDevice = deviceRepository.findById(deviceId)
            .orElseThrow { DeviceNotFoundException(deviceId) }

        return metricRepository
            .findAllByDeviceId(storedDevice.id, pageable)
            .map { metric -> metric.toMetricOutputDTO() }
            .toPageDTO()
    }

    /**
     * Gets the people count of a device.
     *
     * This method will continuously retrieve
     * the latest people count of a device until the device is no longer active.
     *
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
}
