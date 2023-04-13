package com.isel.sensiflow.services.dto.output

import com.isel.sensiflow.model.dao.ProcessedStream
import com.isel.sensiflow.services.ID

interface ProcessedStreamOutputDTO {
    val streamUrl: String
}

data class ProcessedStreamSimpleOutputDTO(
    val deviceID: ID,
    override val streamUrl: String
) : ProcessedStreamOutputDTO

data class ProcessedStreamExpandedOutputDTO(
    override val streamUrl: String,
    val device: DeviceSimpleOutputDTO
) : ProcessedStreamOutputDTO

fun ProcessedStream.toProcessedStreamOutputDTO(expanded: Boolean): ProcessedStreamOutputDTO {
    return if (expanded)
        ProcessedStreamExpandedOutputDTO(
            streamUrl = this.streamURL,
            device = this.device.toDeviceOutputDTO(expanded = false) as DeviceSimpleOutputDTO
        )
    else
        ProcessedStreamSimpleOutputDTO(
            deviceID = this.id,
            streamUrl = this.streamURL
        )
}
