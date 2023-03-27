package com.isel.sensiflow.http.controller

import com.isel.sensiflow.services.DeviceService
import com.isel.sensiflow.services.dto.DeviceInputDTO
import com.isel.sensiflow.services.dto.DeviceOutputDTO
import com.isel.sensiflow.services.dto.DeviceStateInputDTO
import com.isel.sensiflow.services.dto.DeviceUpdateDTO
import com.isel.sensiflow.services.dto.PageDTO
import com.isel.sensiflow.services.dto.PaginationInfo
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import com.isel.sensiflow.services.dto.input.DeviceInputDTO
import com.isel.sensiflow.services.dto.input.DeviceUpdateDTO
import com.isel.sensiflow.services.dto.output.DeviceOutputDTO
import com.isel.sensiflow.services.dto.output.MetricOutputDTO
import com.isel.sensiflow.services.dto.output.PageDTO
import org.springframework.http.ResponseEntity
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
import java.net.URI

@RestController
@RequestMapping(RequestPaths.Device.DEVICE)
class DeviceController(
    val deviceService: DeviceService
) {

    @GetMapping
    fun getDevices(
        @RequestParam page: Int?,
        @RequestParam size: Int?,
        @RequestParam expanded: Boolean = false
    ): PageDTO<DeviceOutputDTO> {
        return deviceService.getAllDevices(PaginationInfo(page, size), expanded = expanded)
    }

    @PostMapping
    fun createDevice(
        @Valid @RequestBody deviceInputDTO: DeviceInputDTO,
        userID: Int?/* TODO Injected by auth */
    ): ResponseEntity<Unit> {
        val createdDevice = deviceService.createDevice(deviceInputDTO, userID ?: 0)

        val locationPath = (RequestPaths.Device.DEVICE + "/%d").format(createdDevice.id)

        return ResponseEntity
            .created(URI(locationPath))
            .build()
    }

    @GetMapping(RequestPaths.Device.DEVICE_ID)
    fun getDevice(
        @PathVariable id: Int,
        @RequestParam expanded: Boolean = false
    ): DeviceOutputDTO {
        return deviceService.getDeviceById(id, expanded)
    }

    @PutMapping(RequestPaths.Device.DEVICE_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateDevice(
        @PathVariable id: Int,
        @Valid @RequestBody deviceInputDTO: DeviceUpdateDTO,
        userID: Int?/* TODO: Injected by auth */
    ) {
        deviceService.updateDevice(id, deviceInputDTO, userID ?: 0)
    }

    @DeleteMapping(RequestPaths.Device.DEVICE_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDevice(@PathVariable id: Int, userID: Int?/* TODO: Injected by auth */) {
        deviceService.deleteDevice(id, userID ?: 0)
    }

    @PutMapping(RequestPaths.Device.PROCESSING_STATE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateProcessingState(
        @PathVariable id: Int,
        @RequestBody deviceStateInputDTO: DeviceStateInputDTO,
        userID: Int?/* TODO: Injected by auth */
    ) {
        deviceService.updateProcessingState(id, deviceStateInputDTO.state, userID ?: 0)
    }

    @GetMapping(RequestPaths.Device.DEVICE_STATS)
    // TODO: @Authentication
    fun getDeviceStats(
        @PathVariable id: Int,
        @RequestParam page: Int,
        @RequestParam size: Int,
        userID: Int /* TODO Injected by auth */
    ): PageDTO<MetricOutputDTO> {
        return deviceService
            .getDeviceStats(PaginationInfo(page, size), id, userID)
    }
}
