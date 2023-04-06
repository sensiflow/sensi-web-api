package com.isel.sensiflow.http.controller

import com.isel.sensiflow.http.pipeline.authentication.Authentication
import com.isel.sensiflow.services.DeviceService
import com.isel.sensiflow.services.dto.PaginationInfo
import com.isel.sensiflow.services.dto.input.DeviceInputDTO
import com.isel.sensiflow.services.dto.input.DeviceStateInputDTO
import com.isel.sensiflow.services.dto.input.DeviceUpdateDTO
import com.isel.sensiflow.services.dto.output.DeviceOutputDTO
import com.isel.sensiflow.services.dto.output.IDOutputDTO
import com.isel.sensiflow.services.dto.output.MetricOutputDTO
import com.isel.sensiflow.services.dto.output.PageDTO
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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

    @Authentication
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createDevice(
        @Valid @RequestBody deviceInputDTO: DeviceInputDTO,
        userID: Int
    ): IDOutputDTO {
        val deviceID = deviceService
            .createDevice(deviceInputDTO, userID)
            .id

        return IDOutputDTO(deviceID)
    }

    @GetMapping(RequestPaths.Device.DEVICE_ID)
    fun getDevice(
        @PathVariable id: Int,
        @RequestParam expanded: Boolean = false
    ): DeviceOutputDTO {
        return deviceService.getDeviceById(id, expanded)
    }

    @Authentication
    @PutMapping(RequestPaths.Device.DEVICE_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateDevice(
        @PathVariable id: Int,
        @Valid @RequestBody deviceInputDTO: DeviceUpdateDTO,
        userID: Int
    ) {
        deviceService.updateDevice(id, deviceInputDTO, userID)
    }

    @Authentication
    @DeleteMapping(RequestPaths.Device.DEVICE_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDevice(@PathVariable id: Int, userID: Int) {
        deviceService.deleteDevice(id, userID)
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

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(RequestPaths.Device.DEVICE_STATS)
    @Authentication
    fun getDeviceStats(
        @PathVariable id: Int,
        @RequestParam page: Int?,
        @RequestParam size: Int?,
        userID: Int
    ): PageDTO<MetricOutputDTO> {
        return deviceService
            .getDeviceStats(PaginationInfo(page, size), id, userID)
    }
}
