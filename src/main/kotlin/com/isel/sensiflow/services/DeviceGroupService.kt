package com.isel.sensiflow.services

import com.isel.sensiflow.model.dao.DeviceGroup
import com.isel.sensiflow.model.repository.DeviceGroupRepository
import com.isel.sensiflow.model.repository.DeviceRepository
import com.isel.sensiflow.services.dto.PaginationInfo
import com.isel.sensiflow.services.dto.input.DevicesGroupInputDTO
import com.isel.sensiflow.services.dto.input.DevicesGroupUpdateDTO
import com.isel.sensiflow.services.dto.output.DeviceOutputDTO
import com.isel.sensiflow.services.dto.output.PageDTO
import com.isel.sensiflow.services.dto.output.toDTO
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class DeviceGroupService(
    val deviceGroupRepository: DeviceGroupRepository,
    private val deviceRepository: DeviceRepository,
) {
    /**
     * Updates a device group.
     * @param groupId The id of the group to update
     * @param groupInput The input data to update the group
     * @throws DeviceGroupNotFoundException If the group does not exist
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun updateGroup(groupId: Int, groupInput: DevicesGroupUpdateDTO): DeviceGroup {
        val deviceGroup = deviceGroupRepository.findById(groupId)
            .orElseThrow { DeviceGroupNotFoundException(groupId) }

        val groupName = groupInput.name ?: deviceGroup.name

        val groupDescription = groupInput.description?.check() ?: deviceGroup.description

        return deviceGroupRepository.save(
            DeviceGroup(deviceGroup.id, groupName, groupDescription)
        )
    }

    /**
     * Deletes a device group.
     * @param groupID The id of the group to delete
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun deleteGroup(groupID: Int) {
        deviceGroupRepository.findById(groupID)
            .orElseThrow { DeviceGroupNotFoundException(groupID) }

        deviceGroupRepository.deleteById(groupID)
    }

    /**
     * Updates the list of devices in a group.
     * @param groupID The id of the group to update
     * @param input The input data with the new list of devices
     * @throws DeviceGroupNotFoundException If the group does not exist
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun updateDevicesGroup(groupID: Int, input: DevicesGroupInputDTO): DeviceGroup {
        val group = deviceGroupRepository.findById(groupID)
            .orElseThrow { DeviceGroupNotFoundException(groupID) }

        val newDevices = deviceRepository.findAllById(input.deviceIDs)

        group.devices.clear()
        group.devices.addAll(newDevices)
        return group
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun getDevicesFromGroup(groupID: Int, paginationInfo: PaginationInfo): PageDTO<DeviceOutputDTO> {
        val pageable: Pageable = PageRequest.of(paginationInfo.page, paginationInfo.size)

        deviceGroupRepository.findById(groupID)
            .orElseThrow { DeviceGroupNotFoundException(groupID) }

        return deviceGroupRepository
            .findAllDevicesByGroupId(groupID, pageable)
            .map { it.toDTO(expanded = false) }
            .toDTO()
    }
}

// TODO: Move to an utils file / use ycdh's utils function
fun String.check(): String? {
    return this.ifBlank { null }
}
