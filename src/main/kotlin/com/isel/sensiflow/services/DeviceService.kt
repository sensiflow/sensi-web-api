package com.isel.sensiflow.services

import com.isel.sensiflow.Constants
import com.isel.sensiflow.model.dao.Device
import com.isel.sensiflow.model.dao.DeviceProcessingState
import com.isel.sensiflow.model.repository.DeviceRepository
import com.isel.sensiflow.model.repository.UserRepository
import com.isel.sensiflow.services.dto.DeviceInputDTO
import com.isel.sensiflow.services.dto.DeviceOutputDTO
import com.isel.sensiflow.services.dto.DeviceUpdateDTO
import com.isel.sensiflow.services.dto.PageDTO
import com.isel.sensiflow.services.dto.PaginationInfo
import com.isel.sensiflow.services.dto.isEmpty
import com.isel.sensiflow.services.dto.isEqual
import com.isel.sensiflow.services.dto.toDTO
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class DeviceService(
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository
) {

    /**
     * Creates a new device.
     * @param deviceInput The input data for the device.
     * @param userId The id of the user that owns the device.
     * @return The created device.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun createDevice(deviceInput: DeviceInputDTO, userId: Int): Device {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException(userId) }
        val newDevice = Device(
            name = deviceInput.name,
            streamURL = deviceInput.streamURL,
            description = deviceInput.description ?: "",
            user = user
        )

        return deviceRepository.save(newDevice)
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
     * Updates a device's information.
     *
     * All provided fields are overwritten, except if in the input the field is null.
     *
     * @param deviceId The id of the device.
     * @param deviceInput The input data for the device.
     * @param userId The id of the user that owns the device.
     * @return The updated device.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun updateDevice(deviceId: Int, deviceInput: DeviceUpdateDTO, userId: Int): Device {

        val device = deviceRepository.findById(deviceId)
            .orElseThrow { DeviceNotFoundException(deviceId) }

        if (device.user.id != userId)
            throw OwnerMismatchException(Constants.Error.DEVICE_OWNER_MISMATCH.format(deviceId, userId))

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
     * @param userId The id of the user that owns the device.
     * @throws DeviceNotFoundException If the device does not exist.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun deleteDevice(deviceId: Int, userId: Int) {
        val device = deviceRepository.findById(deviceId)
            .orElseThrow { DeviceNotFoundException(deviceId) }

        if (device.user.id != userId)
            throw OwnerMismatchException(
                Constants.Error.DEVICE_OWNER_MISMATCH.format(deviceId, userId)
            )

        deviceRepository.deleteById(deviceId)
    }

    /**
     * Changes the processing state of a device.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun updateProcessingState(deviceID: Int, state: String?, userID: Int) {

        val safeState = state ?: throw InvalidProcessingStateException("null")
        val newProcessingState = DeviceProcessingState.fromString(safeState)
            ?: throw InvalidProcessingStateException(safeState)

        val device = deviceRepository.findById(deviceID)
            .orElseThrow { DeviceNotFoundException(deviceID) }

        if (device.user.id != userID)
            throw OwnerMismatchException(Constants.Error.DEVICE_OWNER_MISMATCH.format(deviceID, userID))

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
}
