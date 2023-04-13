package com.isel.sensiflow.services

import com.isel.sensiflow.model.repository.DeviceRepository
import com.isel.sensiflow.model.repository.ProcessedStreamRepository
import com.isel.sensiflow.services.dto.output.ProcessedStreamOutputDTO
import com.isel.sensiflow.services.dto.output.toProcessedStreamOutputDTO
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
     * @param expanded True if it should return the device too
     * @throws DeviceNotFoundException If the device doesn't exist
     * @throws ProcessedStreamNotFoundException If the processed stream doesn't exist
     * @return The processed stream of the device
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun getProcessedStreamOfDeviceWith(deviceId: Int, expanded: Boolean): ProcessedStreamOutputDTO {
        deviceRepository.findById(deviceId)
            .orElseThrow { DeviceNotFoundException(deviceId) }

        val processedStream = processedStreamRepository.findById(deviceId)
            .orElseThrow { ProcessedStreamNotFoundException(deviceId) }

        return processedStream.toProcessedStreamOutputDTO(expanded)
    }
}
