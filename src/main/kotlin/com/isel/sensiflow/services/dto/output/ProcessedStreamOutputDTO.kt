package com.isel.sensiflow.services.dto.output

import com.isel.sensiflow.model.dao.ProcessedStream

interface ProcessedStreamOutputDTO {
    val deviceId: Int
    val streamUrl: String
}

data class ProcessedStreamSimpleOutputDTO(
    override val deviceId: Int,
    override val streamUrl: String
) : ProcessedStreamOutputDTO

data class ProcessedStreamExpandedOutputDTO(
    override val deviceId: Int,
    override val streamUrl: String,
    val device: DeviceOutputDTO,
) : ProcessedStreamOutputDTO

fun ProcessedStream.toDTO(expanded: Boolean): ProcessedStreamOutputDTO {
    return if (expanded)
        ProcessedStreamExpandedOutputDTO(
            deviceId = this.id,
            streamUrl = this.streamURL,
            device = this.device.toDTO(expanded = false)
        )
    else
        ProcessedStreamSimpleOutputDTO(
            deviceId = this.id,
            streamUrl = this.streamURL
        )
}
