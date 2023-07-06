package com.isel.sensiflow.http.controller

import com.isel.sensiflow.http.pipeline.authentication.Authentication
import com.isel.sensiflow.http.utils.launchServerSentEvent
import com.isel.sensiflow.services.ID
import com.isel.sensiflow.services.Role
import com.isel.sensiflow.services.beans.DeviceProcessingStateService
import com.isel.sensiflow.services.dto.input.DeviceStateInputDTO
import jakarta.validation.Valid
import kotlinx.coroutines.flow.onCompletion
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
class DeviceProcessingStateController(
    val processingStateService: DeviceProcessingStateService
) {

    @Authentication(authorization = Role.MODERATOR)
    @PutMapping(RequestPaths.Device.DEVICE + RequestPaths.Device.PROCESSING_STATE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun updateProcessingState(
        @PathVariable id: Int,
        @RequestBody @Valid deviceStateInputDTO: DeviceStateInputDTO,
    ) {
        processingStateService.startUpdateProcessingState(id, deviceStateInputDTO.state)
    }

    @RequestMapping(
        RequestPaths.Device.DEVICE + RequestPaths.Device.DEVICE_ID + RequestPaths.SSE.SSE_DEVICE_STATE,
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    @Authentication(Role.USER)
    fun subscribeToChangeOfDeviceState(@PathVariable("id") id: ID): SseEmitter {
        return launchServerSentEvent { sseEmitter ->
            processingStateService.getDeviceStateFlow(id)
                .onCompletion { cause ->
                    if (cause != null) {
                        sseEmitter.completeWithError(cause)
                    } else
                        sseEmitter.complete()
                }
                .collect { value ->
                    val event = SseEmitter.event()
                        .name("device-state")
                        .data(value)

                    sseEmitter.send(
                        event
                    )
                }
        }
    }
}
