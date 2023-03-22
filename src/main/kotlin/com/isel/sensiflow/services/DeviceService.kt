package com.isel.sensiflow.services

import com.isel.sensiflow.Constants
import com.isel.sensiflow.model.entities.Device
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
            streamurl = deviceInput.streamUrl,
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
            streamurl = deviceInput.streamUrl ?: device.streamurl,
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
}
