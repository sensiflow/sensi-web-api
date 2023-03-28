package com.isel.sensiflow.services

import com.isel.sensiflow.model.dao.DeviceGroup
import com.isel.sensiflow.model.repository.DeviceGroupRepository
import com.isel.sensiflow.model.repository.DeviceRepository
import com.isel.sensiflow.services.dto.PaginationInfo
import com.isel.sensiflow.services.dto.input.DevicesGroupInputDTO
import com.isel.sensiflow.services.dto.input.DevicesGroupUpdateDTO
import com.isel.sensiflow.services.dto.output.DeviceGroupOutputDTO
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
     * @param groupID The id of the group to update
     * @param groupInput The input data to update the group
     * @throws DeviceGroupNotFoundException If the group does not exist
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun updateGroup(groupID: ID, groupInput: DevicesGroupUpdateDTO): DeviceGroup {
        val deviceGroup = deviceGroupRepository.findById(groupID)
            .orElseThrow { DeviceGroupNotFoundException(groupID) }

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
    fun deleteGroup(groupID: ID) {
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
    fun updateDevicesGroup(groupID: ID, input: DevicesGroupInputDTO): DeviceGroup {
        val group = deviceGroupRepository.findById(groupID)
            .orElseThrow { DeviceGroupNotFoundException(groupID) }

        val newDevices = deviceRepository.findAllById(input.deviceIDs)

        group.devices.clear()
        group.devices.addAll(newDevices)
        return group
    }

    /**
     * Gets the list of devices in a group. The list can be paginated. The list can be expanded or not.
     * @param groupID The id of the group to get the devices from
     * @param paginationInfo The pagination info
     * @param expanded If the list should be expanded or not
     * @return The list of devices in the group as [PageDTO] of [DeviceOutputDTO]
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun getDevicesFromGroup(groupID: ID, paginationInfo: PaginationInfo, expanded: Boolean): PageDTO<DeviceOutputDTO> {
        val pageable: Pageable = PageRequest.of(paginationInfo.page, paginationInfo.size)

        deviceGroupRepository.findById(groupID)
            .orElseThrow { DeviceGroupNotFoundException(groupID) }

        return deviceGroupRepository
            .findAllDevicesByGroupId(groupID, pageable)
            .map { it.toDTO(expanded = expanded) }
            .toDTO()
    }

    /**
     * Gets a device group.
     * @param groupID The id of the group to get
     * @param expanded If the group should be expanded or not
     * @return The device group as [DeviceGroupOutputDTO]
     * @throws DeviceGroupNotFoundException If the group does not exist
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun getGroup(groupID: ID, expanded: Boolean): DeviceGroupOutputDTO {
        return deviceGroupRepository
            .findById(groupID)
            .orElseThrow { DeviceGroupNotFoundException(groupID) }
            .toDTO(expanded)
    }
}