package com.isel.sensiflow.services

import com.isel.sensiflow.model.dao.Device
import com.isel.sensiflow.model.dao.DeviceProcessingState
import com.isel.sensiflow.model.repository.DeviceRepository
import com.isel.sensiflow.model.repository.MetricRepository
import com.isel.sensiflow.model.repository.UserRepository
import com.isel.sensiflow.services.dto.PaginationInfo
import com.isel.sensiflow.services.dto.input.DeviceInputDTO
import com.isel.sensiflow.services.dto.input.DeviceUpdateDTO
import com.isel.sensiflow.services.dto.input.isEmpty
import com.isel.sensiflow.services.dto.input.isEqual
import com.isel.sensiflow.services.dto.output.DeviceOutputDTO
import com.isel.sensiflow.services.dto.output.MetricOutputDTO
import com.isel.sensiflow.services.dto.output.PageDTO
import com.isel.sensiflow.services.dto.output.toDTO
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class DeviceService(
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository,
    private val metricRepository: MetricRepository
) {

    /**
     * Creates a new device.
     * @param deviceInput The input data for the device.
     * @param userId The id of the user that owns the device.
     * @return The created device.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun createDevice(deviceInput: DeviceInputDTO, userId: Int): DeviceOutputDTO {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException(userId) }
        val newDevice = Device(
            name = deviceInput.name,
            streamURL = deviceInput.streamURL,
            description = deviceInput.description ?: "",
            user = user
        )

        return deviceRepository
            .save(newDevice)
            .toDTO(expanded = false)
    }

    /**
     * Gets a device by its id.
     * @param deviceId The id of the device.
     * @param expanded If the device should contain embedded entities.
     * @return The requested device.
     * @throws DeviceNotFoundException If the device does not exist.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun getDeviceById(deviceId: Int, expanded: Boolean): DeviceOutputDTO {
        return deviceRepository.findById(deviceId)
            .orElseThrow { DeviceNotFoundException(deviceId) }
            .toDTO(expanded)
    }

    /**
     * Gets all devices.
     * @param paginationInfo The pagination information.
     * @return A [PageDTO] of devices.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun getAllDevices(paginationInfo: PaginationInfo, expanded: Boolean): PageDTO<DeviceOutputDTO> {
        val pageable: Pageable = PageRequest.of(paginationInfo.page, paginationInfo.size)
        return deviceRepository
            .findAll(pageable)
            .map { it.toDTO(expanded = expanded) }
            .toDTO()
    }

    /**
     * Updates a device.
     *
     * All provided fields are overwritten, except if in the input the field is null.
     *
     * @param deviceId The id of the device.
     * @param deviceInput The input data for the device to update.
     * @return The updated [Device].
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun updateDevice(deviceId: Int, deviceInput: DeviceUpdateDTO): Device {

        val device = deviceRepository.findById(deviceId)
            .orElseThrow { DeviceNotFoundException(deviceId) }

        // If the input is empty, there is nothing to update.
        // Same if the input is the same as the current device.
        if (deviceInput.isEmpty() || device.isEqual(deviceInput)) return device

        val updatedDevice = Device(
            id = device.id,
            name = deviceInput.name ?: device.name,
            streamURL = deviceInput.streamURL ?: device.streamURL,
            description = deviceInput.description ?: device.description,
            user = device.user
        )

        return deviceRepository.save(updatedDevice)
    }

    /**
     * Deletes a device.
     * @param deviceId The id of the device.
     * @throws DeviceNotFoundException If the device does not exist.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun deleteDevice(deviceId: Int) {
        deviceRepository.findById(deviceId)
            .orElseThrow { DeviceNotFoundException(deviceId) }

        deviceRepository.deleteById(deviceId)
    }

    /**
     * Changes the processing state of a device.
     * @param deviceID The id of the device.
     * @param state The new state of the device.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun updateProcessingState(deviceID: Int, state: String?) {

        val safeState = state ?: throw InvalidProcessingStateException("null")
        val newProcessingState = DeviceProcessingState.fromString(safeState)
            ?: throw InvalidProcessingStateException(safeState)

        val device = deviceRepository.findById(deviceID)
            .orElseThrow { DeviceNotFoundException(deviceID) }

        if (!device.processingState.isValidTransition(newProcessingState))
            throw InvalidProcessingStateTransitionException(from = device.processingState, to = newProcessingState)

        val newDevice = Device( // Preserve immutability
            id = device.id,
            name = device.name,
            streamURL = device.streamURL,
            description = device.description,
            user = device.user,
            processingState = newProcessingState
        )

        deviceRepository.save(newDevice)
    }

    /**
     * Gets the stats of a device.
     * @param paginationInfo The pagination information.
     * @param deviceId The id of the device.
     * @throws DeviceNotFoundException If the device does not exist.
     * @throws OwnerMismatchException If the user does not own the device.
     * @return A [PageDTO] of [MetricOutputDTO].
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun getDeviceStats(paginationInfo: PaginationInfo, deviceId: Int): PageDTO<MetricOutputDTO> {
        val pageable: Pageable = PageRequest.of(paginationInfo.page, paginationInfo.size)

        val device = deviceRepository.findById(deviceId)
            .orElseThrow { DeviceNotFoundException(deviceId) }

        return metricRepository
            .findAllByDeviceID(device, pageable)
            .map { it.toDTO() }
            .toDTO()
    }
}
