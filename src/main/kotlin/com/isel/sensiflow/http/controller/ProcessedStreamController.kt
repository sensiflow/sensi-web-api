package com.isel.sensiflow.http.controller

import com.isel.sensiflow.http.pipeline.authentication.Authentication
import com.isel.sensiflow.services.ProcessedStreamService
import com.isel.sensiflow.services.Role.USER
import com.isel.sensiflow.services.dto.output.ProcessedStreamOutputDTO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(RequestPaths.Device.DEVICE)
class ProcessedStreamController(val processedStreamService: ProcessedStreamService) {

    @GetMapping(RequestPaths.Device.DEVICE_PROCESSED_STREAM)
    @Authentication(authorization = USER)
    fun getProcessedStream(
        @PathVariable id: Int,
        @RequestParam expanded: Boolean = false
    ): ProcessedStreamOutputDTO {
        return processedStreamService
            .getProcessedStreamOfDeviceWith(id, expanded)
    }
}
