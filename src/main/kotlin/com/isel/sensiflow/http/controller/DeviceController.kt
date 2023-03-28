package com.isel.sensiflow.http.controller

import com.isel.sensiflow.http.pipeline.authentication.Authentication
import com.isel.sensiflow.services.DeviceService
import com.isel.sensiflow.services.dto.PaginationInfo
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
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping(RequestPaths.Device.DEVICE)
class DeviceController(
    val deviceService: DeviceService
) {

    @GetMapping
    fun getDevices(
        @RequestParam page: Int,
        @RequestParam size: Int,
        @RequestParam expanded: Boolean = false
    ): PageDTO<DeviceOutputDTO> {
        return deviceService
            .getAllDevices(PaginationInfo(page, size), expanded = expanded)
    }

    @Authentication
    @PostMapping
    fun createDevice(
        @RequestBody deviceInputDTO: DeviceInputDTO,
        userID: Int
    ): ResponseEntity<Unit> {
        val createdDevice = deviceService.createDevice(deviceInputDTO, userID)

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

    @Authentication
    @PutMapping(RequestPaths.Device.DEVICE_ID)
    fun updateDevice(
        @PathVariable id: Int,
        @RequestBody deviceInputDTO: DeviceUpdateDTO,
        userID: Int
    ): ResponseEntity<Unit> {
        deviceService.updateDevice(id, deviceInputDTO, userID)

        return ResponseEntity
            .noContent()
            .build()
    }

    @Authentication
    @DeleteMapping(RequestPaths.Device.DEVICE_ID)
    fun deleteDevice(@PathVariable id: Int, userID: Int): ResponseEntity<Unit> {
        deviceService.deleteDevice(id, userID)

        return ResponseEntity
            .noContent()
            .build()
    }

    @GetMapping(RequestPaths.Device.DEVICE_STATS)
    @Authentication
    fun getDeviceStats(
        @PathVariable id: Int,
        @RequestParam page: Int,
        @RequestParam size: Int,
        userID: Int
    ): PageDTO<MetricOutputDTO> {
        return deviceService
            .getDeviceStats(PaginationInfo(page, size), id, userID)
    }
}
