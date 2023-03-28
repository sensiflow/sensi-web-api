package com.isel.sensiflow.http.controller

import com.isel.sensiflow.http.pipeline.authentication.Authentication
import com.isel.sensiflow.services.DeviceGroupService
import com.isel.sensiflow.services.UserID
import com.isel.sensiflow.services.dto.PaginationInfo
import com.isel.sensiflow.services.dto.input.DevicesGroupInputDTO
import com.isel.sensiflow.services.dto.input.DevicesGroupUpdateDTO
import com.isel.sensiflow.services.dto.output.DeviceGroupOutputDTO
import com.isel.sensiflow.services.dto.output.DeviceOutputDTO
import com.isel.sensiflow.services.dto.output.PageDTO
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(RequestPaths.DeviceGroups.GROUP)
class DeviceGroupController(val deviceGroupService: DeviceGroupService) {

    @GetMapping(RequestPaths.DeviceGroups.GROUP_ID)
    fun getGroup(
        @PathVariable id: Int,
        @RequestParam expanded: Boolean = false
    ): DeviceGroupOutputDTO {
        return deviceGroupService.getGroup(id, expanded)
    }

    @Authentication
    @PutMapping(RequestPaths.DeviceGroups.GROUP_ID)
    fun updateGroup(
        @PathVariable id: Int,
        @RequestBody @Valid inputDTO: DevicesGroupUpdateDTO,
        userID: UserID
    ): ResponseEntity<Unit> {
        deviceGroupService.updateGroup(id, inputDTO)

        return ResponseEntity
            .noContent()
            .build()
    }

    @Authentication
    @DeleteMapping(RequestPaths.DeviceGroups.GROUP_ID)
    fun deleteGroup(
        @PathVariable id: Int,
        userID: UserID
    ): ResponseEntity<Unit> {
        deviceGroupService.deleteGroup(id)

        return ResponseEntity
            .noContent()
            .build()
    }

    @Authentication
    @PutMapping(RequestPaths.DeviceGroups.GROUPS_DEVICES)
    fun updateDevicesGroup(
        @PathVariable id: Int,
        @RequestBody inputDTO: DevicesGroupInputDTO,
        userID: UserID
    ): ResponseEntity<Unit> {
        deviceGroupService.updateDevicesGroup(id, inputDTO)

        return ResponseEntity
            .noContent()
            .build()
    }

    @GetMapping(RequestPaths.DeviceGroups.GROUPS_DEVICES)
    fun getDevicesFromGroup(
        @PathVariable id: Int,
        @RequestParam page: Int,
        @RequestParam size: Int,
        @RequestParam expanded: Boolean = false
    ): PageDTO<DeviceOutputDTO> =
        deviceGroupService
            .getDevicesFromGroup(id, PaginationInfo(page, size), expanded)
}
