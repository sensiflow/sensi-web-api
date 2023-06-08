package com.isel.sensiflow.services.dto.output

import com.isel.sensiflow.model.entities.Device
import com.isel.sensiflow.services.ID

interface ProcessedStreamOutputDTO {
    val processedStreamUrl: String?
}

data class ProcessedStreamSimpleOutputDTO(
    val deviceID: ID,
    override val processedStreamUrl: String?
) : ProcessedStreamOutputDTO

data class ProcessedStreamExpandedOutputDTO(
    override val processedStreamUrl: String?,
    val device: DeviceSimpleOutputDTO
) : ProcessedStreamOutputDTO

fun Device.toProcessedStreamOutputDTO(expanded: Boolean): ProcessedStreamOutputDTO {
    return if (expanded)
        ProcessedStreamExpandedOutputDTO(
            processedStreamUrl = this.processedStreamURL,
            device = this.toDeviceOutputDTO(expanded = false) as DeviceSimpleOutputDTO
        )
    else
        ProcessedStreamSimpleOutputDTO(
            deviceID = this.id,
            processedStreamUrl = this.processedStreamURL
        )
}
