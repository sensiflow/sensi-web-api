package com.isel.sensiflow.services

import com.isel.sensiflow.Constants
import com.isel.sensiflow.model.entities.Device
import com.isel.sensiflow.model.entities.DeviceGroup
import com.isel.sensiflow.model.entities.User
import com.isel.sensiflow.model.entities.Userrole
import com.isel.sensiflow.model.repository.DeviceGroupRepository
import com.isel.sensiflow.model.repository.DeviceRepository
import com.isel.sensiflow.services.dto.PageableDTO
import com.isel.sensiflow.services.dto.input.DevicesGroupCreateDTO
import com.isel.sensiflow.services.dto.input.DevicesGroupInputDTO
import com.isel.sensiflow.services.dto.input.DevicesGroupUpdateDTO
import com.isel.sensiflow.services.dto.output.toDeviceGroupOutputDTO
import com.isel.sensiflow.services.dto.output.toDeviceOutputDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional

@RunWith(MockitoJUnitRunner::class)
class DeviceGroupTests {

    @InjectMocks
    private lateinit var deviceGroupService: DeviceGroupService

    @Mock
    private lateinit var deviceGroupRepository: DeviceGroupRepository

    @Mock
    private lateinit var deviceRepository: DeviceRepository

    @BeforeEach
    fun initMocks() {
        MockitoAnnotations.openMocks(this)
    }

    private val ADMINRole = Userrole(
        id = 1,
        role = Role.ADMIN.name
    )

    private val fakeUserOwner = User(
        id = 1,
        firstName = "John",
        lastName = "Doe",
        role = ADMINRole,
        passwordHash = "hash",
        passwordSalt = "salt"
    )

    private val fakeDeviceGroup = DeviceGroup(
        id = 1,
        name = "fake name",
        description = "fake description"
    )

    private val fakeDevice = Device(
        id = 1,
        name = "Device 1",
        streamURL = "https://example.com/device1/stream",
        description = "Device 1 description"
    )

    private val fakeDevice2 = Device(
        id = 1,
        name = "Device 2",
        streamURL = "https://example.com/device2/stream",
        description = "Device 2 description"
    )

    @Test
    fun `device group update successfully`() {
        // Arrange
        val deviceGroupID = 1

        `when`(deviceGroupRepository.findById(deviceGroupID)).thenReturn(Optional.of(fakeDeviceGroup))

        val updatedGroup = DeviceGroup(
            id = 1,
            name = "Updated name",
            description = "Updated desc"
        )

        val groupDTO = DevicesGroupUpdateDTO(
            name = updatedGroup.name,
            description = updatedGroup.description
        )

        `when`(deviceGroupRepository.save(any(DeviceGroup::class.java))).thenReturn(updatedGroup)

        // Act
        val result = deviceGroupService.updateGroup(deviceGroupID, groupDTO)

        // Assert
        assertEquals(updatedGroup, result)
        verify(deviceGroupRepository, times(1)).findById(deviceGroupID)
        verify(deviceGroupRepository, times(1)).save(any(DeviceGroup::class.java))
    }

    @Test
    fun `update a group that does not exist throws exception`() {
        // Arrange
        val deviceGroupID = 1
        `when`(deviceGroupRepository.findById(deviceGroupID)).thenReturn(Optional.empty())

        val groupDTO = DevicesGroupUpdateDTO(
            name = "",
            description = ""
        )

        // Act
        assertThrows<DeviceGroupNotFoundException> {
            deviceGroupService.updateGroup(deviceGroupID, groupDTO)
        }

        // Assert
        verify(deviceGroupRepository, times(1)).findById(deviceGroupID)
        verify(deviceGroupRepository, times(0)).save(any(DeviceGroup::class.java))
    }

    @Test
    fun `device group update with null description returns same description`() {
        // Arrange
        val deviceGroupID = 1

        `when`(deviceGroupRepository.findById(deviceGroupID)).thenReturn(Optional.of(fakeDeviceGroup))

        val updatedGroup = DeviceGroup(
            id = 1,
            name = "Updated name",
            description = null
        )

        val groupDTO = DevicesGroupUpdateDTO(
            name = updatedGroup.name,
            description = fakeDeviceGroup.description
        )

        `when`(deviceGroupRepository.save(any(DeviceGroup::class.java))).thenReturn(updatedGroup)

        // Act
        val result = deviceGroupService.updateGroup(deviceGroupID, groupDTO)

        // Assert
        assertEquals(updatedGroup, result)
        verify(deviceGroupRepository, times(1)).findById(deviceGroupID)
        verify(deviceGroupRepository, times(1)).save(any(DeviceGroup::class.java))
    }

    @Test
    fun `device group update with blank description returns null description`() {
        // Arrange
        val deviceGroupID = 1
        `when`(deviceGroupRepository.findById(deviceGroupID)).thenReturn(Optional.of(fakeDeviceGroup))

        val updatedGroup = DeviceGroup(
            id = 1,
            name = "Updated name",
            description = ""
        )

        val groupDTO = DevicesGroupUpdateDTO(
            name = updatedGroup.name,
            description = null
        )

        `when`(deviceGroupRepository.save(any(DeviceGroup::class.java))).thenReturn(updatedGroup)

        // Act
        val result = deviceGroupService.updateGroup(deviceGroupID, groupDTO)

        // Assert
        assertEquals(updatedGroup, result)
        verify(deviceGroupRepository, times(1)).findById(deviceGroupID)
        verify(deviceGroupRepository, times(1)).save(any(DeviceGroup::class.java))
    }

    @Test
    fun `delete a device group successfully`() {
        // Arrange
        val deviceGroupID = 1
        `when`(deviceGroupRepository.findById(deviceGroupID)).thenReturn(Optional.of(fakeDeviceGroup))
        doNothing().`when`(deviceGroupRepository).deleteById(deviceGroupID)

        // Act
        deviceGroupService.deleteGroup(deviceGroupID)

        // Assert
        verify(deviceGroupRepository, times(1)).findById(deviceGroupID)
        verify(deviceGroupRepository, times(1)).deleteById(deviceGroupID)
    }

    @Test
    fun `delete device group when group does not exist`() {
        // Arrange
        val nonExistingGroup = 1
        `when`(deviceGroupRepository.findById(nonExistingGroup)).thenReturn(Optional.empty())

        // Act
        assertThrows<DeviceGroupNotFoundException> {
            deviceGroupService.deleteGroup(nonExistingGroup)
        }

        // Assert
        verify(deviceGroupRepository, times(1)).findById(nonExistingGroup)
        verify(deviceGroupRepository, times(0)).deleteById(anyInt())
    }

    @Test
    fun `add devices to a group successfully`() {
        // Arrange
        val deviceGroupID = 1
        val inputList = listOf(fakeDevice.id)

        `when`(deviceGroupRepository.findById(deviceGroupID)).thenReturn(Optional.of(fakeDeviceGroup))
        `when`(deviceRepository.findAllById(listOf(fakeDevice.id))).thenReturn(listOf(fakeDevice))

        // Act
        deviceGroupService.addDevicesToGroup(deviceGroupID, DevicesGroupInputDTO(inputList))

        // Assert
        assertEquals(fakeDeviceGroup.devices, listOf(fakeDevice).toSet())
        verify(deviceGroupRepository, times(1)).findById(deviceGroupID)
        verify(deviceRepository, times(1)).findAllById(listOf(fakeDevice.id))
    }

    @Test
    fun `add devices that were already added to a group`() {
        // Arrange
        val deviceGroupID = 1
        val inputList = listOf(fakeDevice.id)

        `when`(deviceGroupRepository.findById(deviceGroupID)).thenReturn(Optional.of(fakeDeviceGroup))
        `when`(deviceRepository.findAllById(listOf(fakeDevice.id))).thenReturn(listOf(fakeDevice))

        // Act
        deviceGroupService.addDevicesToGroup(deviceGroupID, DevicesGroupInputDTO(inputList))

        assertThrows<InvalidParameterException> {
            deviceGroupService.addDevicesToGroup(deviceGroupID, DevicesGroupInputDTO(inputList))
        }
        // Assert
        assertEquals(fakeDeviceGroup.devices, listOf(fakeDevice).toSet())
        verify(deviceGroupRepository, times(2)).findById(deviceGroupID)
        verify(deviceRepository, times(1)).findAllById(listOf(fakeDevice.id))
    }

    @Test
    fun `add devices to a group that does not exist`() {
        // Arrange
        val inputList = listOf(fakeDevice.id)

        `when`(deviceGroupRepository.findById(fakeDeviceGroup.id)).thenReturn(Optional.empty())

        assertThrows<DeviceGroupNotFoundException> {
            deviceGroupService.addDevicesToGroup(fakeDeviceGroup.id, DevicesGroupInputDTO(inputList))
        }

        verify(deviceGroupRepository, times(1)).findById(fakeDeviceGroup.id)
    }

    @Test
    fun `add a device that does not exist to a group`() {
        // Arrange
        val deviceGroupID = 1
        val inputList = listOf(-1)

        `when`(deviceGroupRepository.findById(deviceGroupID)).thenReturn(Optional.of(fakeDeviceGroup))

        assertThrows<DeviceNotFoundException> {
            deviceGroupService.addDevicesToGroup(fakeDeviceGroup.id, DevicesGroupInputDTO(inputList))
        }

        verify(deviceGroupRepository, times(1)).findById(deviceGroupID)
    }

    @Test
    fun `delete a device from a group`() {
        // Arrange
        val deviceGroupID = 1
        val inputList = listOf(fakeDevice.id)

        `when`(deviceGroupRepository.findById(deviceGroupID)).thenReturn(Optional.of(fakeDeviceGroup))
        `when`(deviceRepository.findAllById(listOf(fakeDevice.id))).thenReturn(listOf(fakeDevice))

        // Act
        deviceGroupService.addDevicesToGroup(deviceGroupID, DevicesGroupInputDTO(inputList))
        deviceGroupService.removeDevicesFromGroup(deviceGroupID, inputList)

        // Assert
        verify(deviceGroupRepository, times(2)).findById(deviceGroupID)
        verify(deviceRepository, times(2)).findAllById(listOf(fakeDevice.id))
        verify(deviceGroupRepository, times(2)).save(fakeDeviceGroup)
    }

    @Test
    fun `delete a device from a group that does not exist`() {
        // Arrange
        val inputList = listOf(fakeDevice.id)

        `when`(deviceGroupRepository.findById(fakeDeviceGroup.id)).thenReturn(Optional.empty())
        `when`(deviceRepository.findAllById(listOf(fakeDevice.id))).thenReturn(listOf(fakeDevice))

        // Act
        assertThrows<DeviceGroupNotFoundException> {
            deviceGroupService.removeDevicesFromGroup(fakeDeviceGroup.id, inputList)
        }

        // Assert
        verify(deviceGroupRepository, times(1)).findById(fakeDeviceGroup.id)
        verify(deviceRepository, times(0)).findAllById(listOf(fakeDevice.id))
        verify(deviceGroupRepository, times(0)).save(fakeDeviceGroup)
    }

    @Test
    fun `get the device list of a group successfully`() {
        // Arrange
        val deviceGroupID = 1
        val pageableDTO = PageableDTO(1, 10)

        val expectedPageItems = listOf<Device>(
            fakeDevice,
            fakeDevice2
        )

        val page = PageImpl(
            expectedPageItems,
            PageRequest.of(pageableDTO.page, pageableDTO.size),
            expectedPageItems.size.toLong()
        )

        `when`(deviceGroupRepository.findById(deviceGroupID)).thenReturn(Optional.of(fakeDeviceGroup))
        `when`(
            deviceGroupRepository
                .findPaginatedByEntityDeviceId(deviceGroupID, PageRequest.of(pageableDTO.page, pageableDTO.size))
        )
            .thenReturn(page)

        val expected = expectedPageItems.map { it.toDeviceOutputDTO(expanded = false) }
        val retrievedStats = deviceGroupService
            .getDevicesFromGroup(deviceGroupID, pageableDTO = pageableDTO, expanded = false)

        // Assert
        assertEquals(expected, retrievedStats.items)
        verify(deviceGroupRepository, times(1))
            .findPaginatedByEntityDeviceId(deviceGroupID, PageRequest.of(pageableDTO.page, pageableDTO.size))
    }

    @Test
    fun `get the list of devices of a group that does not exist`() {
        // Arrange
        val nonExistingGroup = 1
        val pageableDTO = PageableDTO(1, 10)

        `when`(deviceGroupRepository.findById(nonExistingGroup)).thenReturn(Optional.empty())

        assertThrows<DeviceGroupNotFoundException> {
            deviceGroupService.getDevicesFromGroup(nonExistingGroup, pageableDTO = pageableDTO, expanded = false)
        }

        // Assert
        verify(deviceGroupRepository, times(0))
            .findPaginatedByEntityDeviceId(nonExistingGroup, PageRequest.of(pageableDTO.page, pageableDTO.size))
    }

    @Test
    fun `get a device group`() {

        `when`(deviceGroupRepository.findById(fakeDeviceGroup.id)).thenReturn(Optional.of(fakeDeviceGroup))

        val result = deviceGroupService.getGroup(fakeDeviceGroup.id, expanded = false)

        assertEquals(fakeDeviceGroup.toDeviceGroupOutputDTO(expanded = false), result)
        verify(deviceGroupRepository, times(1)).findById(fakeDeviceGroup.id)
    }

    @Test
    fun `get a device group expanded`() {

        `when`(deviceGroupRepository.findById(fakeDeviceGroup.id)).thenReturn(Optional.of(fakeDeviceGroup))

        val result = deviceGroupService.getGroup(fakeDeviceGroup.id, expanded = true)

        assertEquals(
            fakeDeviceGroup.toDeviceGroupOutputDTO(
                expanded = true,
                devicesPaginationModel = PageableDTO(
                    page = Constants.Pagination.DEFAULT_PAGE,
                    size = Constants.Pagination.DEFAULT_PAGE_SIZE
                )
            ),
            result
        )
        verify(deviceGroupRepository, times(1)).findById(fakeDeviceGroup.id)
    }

    @Test
    fun `get a device group that does not exist`() {

        `when`(deviceGroupRepository.findById(fakeDeviceGroup.id)).thenReturn(Optional.empty())

        assertThrows<DeviceGroupNotFoundException> {
            deviceGroupService.getGroup(fakeDeviceGroup.id, expanded = false)
        }

        verify(deviceGroupRepository, times(1)).findById(fakeDeviceGroup.id)
    }

    @Test
    fun `get all groups successfully`() {
        // Arrange
        val pageableDTO = PageableDTO(0, 10)

        val expectedPageItems = listOf<DeviceGroup>(
            fakeDeviceGroup
        )

        val page = PageImpl(
            expectedPageItems,
            PageRequest.of(pageableDTO.page, pageableDTO.size),
            expectedPageItems.size.toLong()
        )

        `when`(deviceGroupRepository.findAll(PageRequest.of(pageableDTO.page, pageableDTO.size))).thenReturn(page)

        val expected = expectedPageItems.map { it.toDeviceGroupOutputDTO(expanded = false) }
        val retrievedStats = deviceGroupService
            .getGroups(pageableDTO = pageableDTO, expanded = false)

        // Assert
        assertEquals(expected, retrievedStats.items)
        verify(deviceGroupRepository, times(1))
            .findAll(PageRequest.of(pageableDTO.page, pageableDTO.size))
    }

    @Test
    fun `create a device group with no description and no devices`() {
        // Arrange
        val groupDTO = DevicesGroupCreateDTO(
            name = "Test group",
        )

        val fakeDeviceGroup = DeviceGroup(
            name = groupDTO.name,
            description = null
        )

        `when`(deviceGroupRepository.save(any(DeviceGroup::class.java))).thenReturn(fakeDeviceGroup)

        // Act
        val result = deviceGroupService.createDevicesGroup(groupDTO)

        // Assert
        assertEquals(fakeDeviceGroup, result)
        verify(deviceGroupRepository, times(1)).save(any(DeviceGroup::class.java))
    }

    @Test
    fun `create a device group with devices`() {

        val groupDTO = DevicesGroupCreateDTO(
            name = "Test group",
        )

        val fakeDeviceGroup = DeviceGroup(
            name = groupDTO.name,
            description = null
        )
        fakeDeviceGroup.devices.addAll(listOf(fakeDevice))

        `when`(deviceGroupRepository.save(any(DeviceGroup::class.java))).thenReturn(fakeDeviceGroup)
        `when`(deviceRepository.findAllById(listOf(fakeDevice.id))).thenReturn(listOf(fakeDevice))

        // Act
        val result = deviceGroupService.createDevicesGroup(groupDTO, listOf(fakeDevice.id))

        // Assert
        assertEquals(fakeDeviceGroup, result)
        verify(deviceGroupRepository, times(1)).save(any(DeviceGroup::class.java))
    }

    @Test
    fun `create a device group with a non existant device`() {

        val groupDTO = DevicesGroupCreateDTO(
            name = "Test group",
        )

        val fakeDeviceGroup = DeviceGroup(
            name = groupDTO.name,
            description = null
        )
        fakeDeviceGroup.devices.addAll(listOf(fakeDevice))

        `when`(deviceGroupRepository.save(any(DeviceGroup::class.java))).thenReturn(fakeDeviceGroup)
        `when`(deviceRepository.findAllById(listOf(fakeDevice.id, 88))).thenThrow(IllegalArgumentException("88"))

        assertThrows<DeviceNotFoundException> {
            deviceGroupService.createDevicesGroup(groupDTO, listOf(fakeDevice.id, 88))
        }

        verify(deviceGroupRepository, times(0)).save(any(DeviceGroup::class.java))
    }
}
