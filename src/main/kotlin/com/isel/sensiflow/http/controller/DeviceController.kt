package com.isel.sensiflow.http.controller

import com.isel.sensiflow.http.entities.output.IDOutput
import com.isel.sensiflow.http.entities.output.toIDOutput
import com.isel.sensiflow.http.pipeline.authentication.Authentication
import com.isel.sensiflow.http.utils.launchServerSentEvent
import com.isel.sensiflow.services.ID
import com.isel.sensiflow.services.Role.ADMIN
import com.isel.sensiflow.services.Role.MODERATOR
import com.isel.sensiflow.services.Role.USER
import com.isel.sensiflow.services.UserID
import com.isel.sensiflow.services.beans.DeviceService
import com.isel.sensiflow.services.dto.PageableDTO
import com.isel.sensiflow.services.dto.TimeIntervalDTO
import com.isel.sensiflow.services.dto.input.DeviceInputDTO
import com.isel.sensiflow.services.dto.input.DeviceUpdateDTO
import com.isel.sensiflow.services.dto.output.DeviceOutputDTO
import com.isel.sensiflow.services.dto.output.MetricOutputDTO
import com.isel.sensiflow.services.dto.output.PageDTO
import jakarta.validation.Valid
import kotlinx.coroutines.flow.onCompletion
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.sql.Timestamp

@RestController
@RequestMapping(RequestPaths.Device.DEVICE)
class DeviceController(
    val deviceService: DeviceService,
) {
    @Authentication(authorization = USER)
    @GetMapping
    fun getDevices(
        @RequestParam page: Int?,
        @RequestParam pageSize: Int?,
        @RequestParam expanded: Boolean = false,
        @RequestParam search: String? = null,
    ): PageDTO<DeviceOutputDTO> {
        return deviceService.getAllDevices(
            PageableDTO(page, pageSize),
            expanded = expanded,
            search = search
        )
    }

    @Authentication(authorization = MODERATOR)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createDevice(
        @Valid @RequestBody deviceInputDTO: DeviceInputDTO,
        userID: UserID
    ): IDOutput {
        val deviceID = deviceService
            .createDevice(deviceInputDTO, userID)
            .id

        return deviceID.toIDOutput()
    }

    @Authentication(authorization = USER)
    @GetMapping(RequestPaths.Device.DEVICE_ID)
    fun getDevice(
        @PathVariable id: Int,
        @RequestParam expanded: Boolean = false
    ): DeviceOutputDTO {
        return deviceService.getDeviceById(id, expanded)
    }

    @Authentication(authorization = MODERATOR)
    @PutMapping(RequestPaths.Device.DEVICE_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateDevice(
        @PathVariable id: Int,
        @Valid @RequestBody deviceInputDTO: DeviceUpdateDTO
    ) {
        deviceService.updateDevice(id, deviceInputDTO)
    }

    @Authentication(authorization = ADMIN)
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDevices(
        @RequestParam deviceIDs: List<ID>
    ) {
        deviceService.deleteDevices(deviceIDs)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(RequestPaths.Device.DEVICE_STATS)
    @Authentication(authorization = USER)
    fun getDeviceStats(
        @PathVariable id: Int,
        @RequestParam page: Int? = null,
        @RequestParam size: Int? = null,
        @RequestParam from: Timestamp? = null,
        @RequestParam to: Timestamp? = null,
    ): PageDTO<MetricOutputDTO> {
        return deviceService
            .getDeviceStats(id, PageableDTO(page, size), TimeIntervalDTO(from, to))
    }

    @GetMapping(
        RequestPaths.Device.PEOPLE_COUNT_STREAM,
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    @Authentication(authorization = USER)
    fun getPeopleCountEvent(@PathVariable id: Int): SseEmitter {

        return launchServerSentEvent { sseEmitter ->
            deviceService.getPeopleCountFlow(id)
                .onCompletion { cause ->
                    if (cause != null)
                        sseEmitter.completeWithError(cause)
                    else
                        sseEmitter.complete()
                }.collect { peopleCount ->

                    val eventBuilder = SseEmitter.event()
                        .name("people-count")
                        .data(peopleCount)

                    sseEmitter.send(eventBuilder)
                }
        }
    }
}
