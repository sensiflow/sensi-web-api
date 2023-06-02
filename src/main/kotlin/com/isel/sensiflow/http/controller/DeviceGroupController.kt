package com.isel.sensiflow.http.controller

import com.isel.sensiflow.http.entities.output.IDOutput
import com.isel.sensiflow.http.entities.output.toIDOutput
import com.isel.sensiflow.http.pipeline.authentication.Authentication
import com.isel.sensiflow.services.Role.*
import com.isel.sensiflow.services.DeviceGroupService
import com.isel.sensiflow.services.ID
import com.isel.sensiflow.services.dto.PageableDTO
import com.isel.sensiflow.services.dto.input.DevicesGroupCreateDTO
import com.isel.sensiflow.services.dto.input.DevicesGroupInputDTO
import com.isel.sensiflow.services.dto.input.DevicesGroupUpdateDTO
import com.isel.sensiflow.services.dto.output.DeviceGroupOutputDTO
import com.isel.sensiflow.services.dto.output.DeviceOutputDTO
import com.isel.sensiflow.services.dto.output.PageDTO
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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

@RestController
@RequestMapping(RequestPaths.DeviceGroups.GROUP)
class DeviceGroupController(val deviceGroupService: DeviceGroupService) {

    @Authentication(authorization = USER)
    @GetMapping(RequestPaths.DeviceGroups.GROUP_ID)
    fun getGroup(
        @PathVariable id: Int,
    ): DeviceGroupOutputDTO {
        return deviceGroupService.getGroup(id)
    }

    @Authentication(authorization = MODERATOR)
    @PutMapping(RequestPaths.DeviceGroups.GROUP_ID)
    fun updateGroup(
        @PathVariable id: Int,
        @RequestBody @Valid inputDTO: DevicesGroupUpdateDTO
    ): ResponseEntity<Unit> {
        deviceGroupService.updateGroup(id, inputDTO)

        return ResponseEntity
            .noContent()
            .build()
    }

    @Authentication(authorization = ADMIN)
    @DeleteMapping(RequestPaths.DeviceGroups.GROUP_ID)
    fun deleteGroup(
        @PathVariable id: Int
    ): ResponseEntity<Unit> {
        deviceGroupService.deleteGroup(id)

        return ResponseEntity
            .noContent()
            .build()
    }

    @Authentication(authorization = MODERATOR)
    @PutMapping(RequestPaths.DeviceGroups.GROUPS_DEVICES)
    fun updateDevicesGroup(
        @PathVariable id: Int,
        @RequestBody @Valid inputDTO: DevicesGroupInputDTO
    ): ResponseEntity<Unit> {
        deviceGroupService.updateDevicesGroup(id, inputDTO)

        return ResponseEntity
            .noContent()
            .build()
    }

    @Authentication(authorization = MODERATOR)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun createDevicesGroup(
        @RequestBody @Valid inputDTO: DevicesGroupCreateDTO,
        @RequestParam devices: List<ID>? = null
    ): IDOutput =
        deviceGroupService.createDevicesGroup(inputDTO, devices).id.toIDOutput()

    @Authentication(authorization = USER)
    @GetMapping(RequestPaths.DeviceGroups.GROUPS_DEVICES)
    fun getDevicesFromGroup(
        @PathVariable id: Int,
        @RequestParam page: Int? = null,
        @RequestParam size: Int? = null,
        @RequestParam expanded: Boolean = false
    ): PageDTO<DeviceOutputDTO> =
        deviceGroupService
            .getDevicesFromGroup(id, PageableDTO(page, size), expanded)
}
