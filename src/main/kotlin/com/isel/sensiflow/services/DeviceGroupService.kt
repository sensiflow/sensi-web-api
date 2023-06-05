package com.isel.sensiflow.services

import com.isel.sensiflow.Constants.Pagination.DEFAULT_PAGE
import com.isel.sensiflow.Constants.Pagination.DEFAULT_PAGE_SIZE
import com.isel.sensiflow.model.entities.DeviceGroup
import com.isel.sensiflow.model.repository.DeviceGroupRepository
import com.isel.sensiflow.model.repository.DeviceRepository
import com.isel.sensiflow.model.repository.requireFindAllById
import com.isel.sensiflow.services.dto.PageableDTO
import com.isel.sensiflow.services.dto.input.DevicesGroupCreateDTO
import com.isel.sensiflow.services.dto.input.DevicesGroupInputDTO
import com.isel.sensiflow.services.dto.input.DevicesGroupUpdateDTO
import com.isel.sensiflow.services.dto.output.DeviceGroupOutputDTO
import com.isel.sensiflow.services.dto.output.DeviceGroupSimpleOutputDTO
import com.isel.sensiflow.services.dto.output.DeviceOutputDTO
import com.isel.sensiflow.services.dto.output.PageDTO
import com.isel.sensiflow.services.dto.output.toDeviceGroupOutputDTO
import com.isel.sensiflow.services.dto.output.toDeviceOutputDTO
import com.isel.sensiflow.services.dto.output.toPageDTO
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
     * @param groupUpdateInput The input data to update the group
     * @throws DeviceGroupNotFoundException If the device group does not exist
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun updateGroup(groupID: ID, groupUpdateInput: DevicesGroupUpdateDTO): DeviceGroup {
        val storedDeviceGroup = deviceGroupRepository.findById(groupID)
            .orElseThrow { DeviceGroupNotFoundException(groupID) }

        val groupName = groupUpdateInput.name ?: storedDeviceGroup.name

        val updatedDescription = groupUpdateInput.description ?: storedDeviceGroup.description

        return deviceGroupRepository.save(
            DeviceGroup(id = storedDeviceGroup.id, name = groupName, description = updatedDescription)
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
     * Adds devices to a group.
     * @param groupID The id of the group to add the devices to
     * @param input The input data with the list of device IDs to add
     * @throws DeviceGroupNotFoundException If the group does not exist
     * @throws DeviceNotFoundException If any of the devices does not exist
     * @throws InvalidParameterException If any of the devices to add is already in the group
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun addDevicesToGroup(groupID: ID, input: DevicesGroupInputDTO): DeviceGroup {
        val group = deviceGroupRepository.findById(groupID)
            .orElseThrow { DeviceGroupNotFoundException(groupID) }

        val isAnyDeviceRepeated = group.devices.any { device ->
            device.id in input.deviceIDs
        }

        if (isAnyDeviceRepeated)
            throw InvalidParameterException("This group already contains one or more of the devices to add")

        val devicesToAdd = deviceRepository.requireFindAllById(input.deviceIDs)

        group.devices.addAll(devicesToAdd.toSet())
        deviceGroupRepository.save(group)
        return group
    }

    /**
     * Removes devices from a group.
     * @param groupID The id of the group to remove the devices from
     * @param deviceIDs The list of device IDs to remove
     * @throws DeviceGroupNotFoundException If the group does not exist
     * @throws DeviceNotFoundException If any of the devices does not exist
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun removeDevicesFromGroup(groupID: ID, deviceIDs: List<Int>): DeviceGroup {
        val group = deviceGroupRepository.findById(groupID)
            .orElseThrow { DeviceGroupNotFoundException(groupID) }

        val devicesToRemove = deviceRepository.requireFindAllById(deviceIDs)

        group.devices.removeAll(devicesToRemove.toSet())
        deviceGroupRepository.save(group)
        return group
    }

    /**
     * Gets the list of devices in a group. The list can be paginated. The list can be expanded or not.
     * @param groupID The id of the group to get the devices from
     * @param pageableDTO The pagination info
     * @param expanded If the list should be expanded or not
     * @return The list of devices in the group as [PageDTO] of [DeviceOutputDTO]
     * @throws DeviceGroupNotFoundException If the group does not exist
     * @throws DeviceNotFoundException If a device in the list does not exist
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    fun getDevicesFromGroup(groupID: ID, pageableDTO: PageableDTO, expanded: Boolean): PageDTO<DeviceOutputDTO> {
        val pageable: Pageable = PageRequest.of(pageableDTO.page, pageableDTO.size)

        deviceGroupRepository.findById(groupID)
            .orElseThrow { DeviceGroupNotFoundException(groupID) }

        return deviceGroupRepository
            .findPaginatedByEntityDeviceId(groupID, pageable)
            .map { it.toDeviceOutputDTO(expanded = expanded) }
            .toPageDTO()
    }

    /**
     * Gets a device group. The group can be expanded or not.
     * @param groupID The id of the group to get
     * @return The device group as [DeviceGroupSimpleOutputDTO]
     * @throws DeviceGroupNotFoundException If the group does not exist
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    fun getGroup(groupID: ID, expanded: Boolean): DeviceGroupOutputDTO {
        return deviceGroupRepository
            .findById(groupID)
            .orElseThrow { DeviceGroupNotFoundException(groupID) }
            .toDeviceGroupOutputDTO(
                expanded = expanded,
                devicesPaginationModel = PageableDTO(
                    page = DEFAULT_PAGE,
                    size = DEFAULT_PAGE_SIZE
                )
            )
    }
    /**
     * Gets all device groups. The list can be paginated. The groups can be expanded or not.
     *
     * @param pageableDTO The pagination info
     * @param expanded If the groups should be expanded or not (include the list of devices)
     * @return The list of device groups as [PageDTO] of [DeviceGroupOutputDTO]
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    fun getGroups(pageableDTO: PageableDTO, expanded: Boolean): PageDTO<DeviceGroupOutputDTO> {
        val pageable: Pageable = PageRequest.of(pageableDTO.page, pageableDTO.size)

        return deviceGroupRepository
            .findAll(pageable)
            .map {
                it.toDeviceGroupOutputDTO(
                    expanded = expanded,
                    devicesPaginationModel = PageableDTO(
                        page = DEFAULT_PAGE,
                        size = DEFAULT_PAGE_SIZE
                    )
                )
            }
            .toPageDTO()
    }

    /**
     * Creates a device group.
     * If a device in the given devices list does not exist, an exception is thrown.
     * @param inputDTO The input data to create the group
     * @param devices The list of devices to add to the group
     * @return The id of the created group
     * @throws DeviceNotFoundException if a given device from the received list does not exist
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun createDevicesGroup(inputDTO: DevicesGroupCreateDTO, devices: List<ID>? = null): DeviceGroup {
        val group = DeviceGroup(name = inputDTO.name, description = inputDTO.description)

        if (devices != null) {
            val foundDevices = deviceRepository.requireFindAllById(devices)
            group.devices.addAll(foundDevices)
        }
        return deviceGroupRepository.save(group)
    }
}
