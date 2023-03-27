package com.isel.sensiflow.services

import com.isel.sensiflow.Constants
import com.isel.sensiflow.model.repository.DeviceRepository
import com.isel.sensiflow.model.repository.ProcessedStreamRepository
import com.isel.sensiflow.services.dto.output.ProcessedStreamOutputDTO
import com.isel.sensiflow.services.dto.output.toDTO
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class ProcessedStreamService(
    val processedStreamRepository: ProcessedStreamRepository,
    val deviceRepository: DeviceRepository
) {
    /**
     * Gets the processed stream of a device or, if expanded, the device too.
     * @param deviceId The id of the device
     * @param userId The id of the user
     * @param expanded True if it should return the device too
     * @throws DeviceNotFoundException If the device doesn't exist
     * @throws OwnerMismatchException If the device doesn't belong to the user
     * @throws ProcessedStreamNotFoundException If the processed stream doesn't exist
     * @return The processed stream of the device
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun getProcessedStreamOfDeviceWith(deviceId: Int, userId: Int, expanded: Boolean): ProcessedStreamOutputDTO {
        val device = deviceRepository.findById(deviceId)
            .orElseThrow { DeviceNotFoundException(deviceId) }

        if (device.user.id != userId)
            throw OwnerMismatchException(Constants.Error.DEVICE_OWNER_MISMATCH.format(deviceId, userId))

        val processedStream = processedStreamRepository.findById(deviceId)
            .orElseThrow { ProcessedStreamNotFoundException(deviceId) }

        return processedStream.toDTO(expanded)
    }
}
