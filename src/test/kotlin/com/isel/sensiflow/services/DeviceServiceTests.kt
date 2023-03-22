package com.isel.sensiflow.services

import com.isel.sensiflow.model.entities.Device
import com.isel.sensiflow.model.entities.User
import com.isel.sensiflow.model.repository.DeviceRepository
import com.isel.sensiflow.model.repository.UserRepository
import com.isel.sensiflow.services.dto.DeviceInputDTO
import com.isel.sensiflow.services.dto.DeviceSimpleOutputDTO
import com.isel.sensiflow.services.dto.DeviceUpdateDTO
import com.isel.sensiflow.services.dto.PaginationInfo
import com.isel.sensiflow.services.dto.toDTO
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
class DeviceServiceTests {

    @InjectMocks
    private lateinit var deviceService: DeviceService

    @Mock
    private lateinit var deviceRepository: DeviceRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun initMocks() {
        MockitoAnnotations.openMocks(this)
    }

    private val fakeUser = User(
        id = 1,
        firstName = "John",
        lastName = "Doe",
        passwordHash = "hash",
        passwordSalt = "salt"
    )

    private val fakeDevice = Device(
        id = 1,
        name = "Device 1",
        streamurl = "https://example.com/device1/stream",
        description = "Device 1 description",
        user = fakeUser
    )

    private val fakeDeviceInput = DeviceInputDTO(
        name = fakeDevice.name,
        streamUrl = fakeDevice.streamurl,
        description = fakeDevice.description
    )

    @Test
    fun `creating a device with an existing user`() {
        val userId = 1

        `when`(userRepository.findById(userId)).thenReturn(Optional.of(fakeUser))
        `when`(deviceRepository.save(any(Device::class.java))).thenReturn(fakeDevice)

        val createdDevice = deviceService.createDevice(fakeDeviceInput, userId)

        assertEquals(fakeDevice, createdDevice)

        verify(userRepository, times(1)).findById(userId)
        verify(deviceRepository, times(1)).save(any(Device::class.java))
    }

    @Test
    fun `creating a device for a user that does not exist throws UserNotFoundException`() {
        val userId = 1

        `when`(userRepository.findById(userId)).thenReturn(Optional.empty())

        assertThrows<UserNotFoundException> {
            deviceService.createDevice(fakeDeviceInput, userId)
        }

        verify(userRepository, times(1)).findById(userId)
        verify(deviceRepository, times(0)).save(any(Device::class.java))
    }

    @Test
    fun `creating a device without a description is allowed`() {
        val userId = 1
        val deviceInputNoDescription = fakeDeviceInput.copy(description = null)
        val device = Device(
            id = fakeDevice.id,
            name = fakeDevice.name,
            streamurl = fakeDevice.streamurl,
            description = "",
            user = fakeUser
        )

        `when`(userRepository.findById(userId)).thenReturn(Optional.of(fakeUser))
        `when`(deviceRepository.save(any(Device::class.java))).thenReturn(device)

        val createdDevice = deviceService.createDevice(deviceInputNoDescription, userId)

        assertEquals(device, createdDevice)

        verify(userRepository, times(1)).findById(userId)
        verify(deviceRepository, times(1)).save(any(Device::class.java))
    }

    @Test
    fun `get simple device by id with`() {
        val deviceId = fakeDevice.id

        `when`(deviceRepository.findById(deviceId)).thenReturn(Optional.of(fakeDevice))

        val retrievedDevice = deviceService.getDeviceById(deviceId, expanded = false)
        val expected = DeviceSimpleOutputDTO(
            id = fakeDevice.id,
            name = fakeDevice.name,
            streamUrl = fakeDevice.streamurl,
            description = fakeDevice.description
        )
        assertEquals(expected, retrievedDevice)
        verify(deviceRepository, times(1)).findById(deviceId)
    }

    @Test
    fun `simple get all devices expanded`() {
        val paginationInfo = PaginationInfo(page = 1, size = 10)
        val devices = listOf(
            Device(
                id = 1,
                name = "Device 1",
                streamurl = "https://example.com/device1/stream",
                description = "Device 1 description",
                user = User(
                    id = 1,
                    firstName = "John",
                    lastName = "Doe",
                    passwordHash = "hash",
                    passwordSalt = "salt"
                )
            ),
            Device(
                id = 2,
                name = "Device 2",
                streamurl = "https://example.com/device2/stream",
                description = "Device 2 description",
                user = User(
                    id = 2,
                    firstName = "Jane",
                    lastName = "Doe",
                    passwordHash = "hash",
                    passwordSalt = "salt"
                )
            )
        )
        val page = PageImpl(devices, PageRequest.of(paginationInfo.page, paginationInfo.size), devices.size.toLong())

        `when`(deviceRepository.findAll(PageRequest.of(paginationInfo.page, paginationInfo.size)))
            .thenReturn(page)

        val retrievedDevices = deviceService.getAllDevices(paginationInfo, expanded = true)

        val expected = devices.map { it.toDTO(expanded = true) }

        assertEquals(expected, retrievedDevices.items)
        verify(deviceRepository, times(1))
            .findAll(PageRequest.of(paginationInfo.page, paginationInfo.size))
    }

    @Test
    fun `simple device update`() {
        // Arrange
        val deviceId = 1
        val userId = 1
        val existingDevice = fakeDevice

        `when`(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice))

        val updatedDevice = Device(
            id = 1,
            name = "Updated Test Device",
            description = "This is an updated test device.",
            streamurl = "https://example.com/device1/stream",
            user = fakeDevice.user
        )
        val deviceDto = DeviceUpdateDTO(
            name = updatedDevice.name,
            streamUrl = updatedDevice.streamurl,
            description = updatedDevice.description
        )

        `when`(deviceRepository.save(any(Device::class.java))).thenReturn(updatedDevice)

        // Act
        val result = deviceService.updateDevice(deviceId, deviceDto, userId = userId)

        // Assert
        assertEquals(updatedDevice, result)
        verify(deviceRepository, times(1)).findById(deviceId)
        verify(deviceRepository, times(1)).save(any(Device::class.java))
    }

    @Test
    fun `update device when device does not exist`() {
        // Arrange
        val deviceId = 1
        `when`(deviceRepository.findById(deviceId)).thenReturn(Optional.empty())

        val deviceDto = DeviceUpdateDTO(
            name = "Updated Test Device",
            streamUrl = "https://example.com/device1/stream",
            description = "description"
        )

        // Act
        assertThrows<DeviceNotFoundException> {
            deviceService.updateDevice(deviceId, deviceDto, userId = -1) // uid Irrelevant
        }

        // Assert
        verify(deviceRepository, times(1)).findById(deviceId)
        verify(deviceRepository, times(0)).save(any(Device::class.java))
    }

    @Test
    fun `update device with a user that is not the owner`() {

        val notOwnerId = fakeUser.id + 1
        val deviceId = fakeDevice.id

        val updater = DeviceUpdateDTO(
            name = "Updated Test Device",
            streamUrl = "https://example.com/device1/stream",
            description = "description"
        )

        `when`(deviceRepository.findById(deviceId)).thenReturn(Optional.of(fakeDevice))

        assertThrows<OwnerMismatchException> {
            deviceService.updateDevice(deviceId, updater, notOwnerId)
        }

        verify(deviceRepository, times(0)).save(any(Device::class.java))
        verify(deviceRepository, times(1)).findById(deviceId)
    }

    @Test
    fun `simple delete device`() {
        // Arrange
        val deviceId = 1

        `when`(deviceRepository.findById(deviceId)).thenReturn(Optional.of(fakeDevice))
        doNothing().`when`(deviceRepository).deleteById(deviceId)

        // Act
        deviceService.deleteDevice(deviceId, fakeDevice.user.id)

        // Assert
        verify(deviceRepository, times(1)).findById(deviceId)
        verify(deviceRepository, times(1)).deleteById(deviceId)
    }

    @Test
    fun `delete device when device does not exist`() {
        // Arrange
        val nonExistingDevice = 1
        `when`(deviceRepository.findById(nonExistingDevice)).thenReturn(Optional.empty())

        // Act
        assertThrows<DeviceNotFoundException> {
            deviceService.deleteDevice(nonExistingDevice, fakeUser.id)
        }

        // Assert
        verify(deviceRepository, times(1)).findById(nonExistingDevice)
        verify(deviceRepository, times(0)).deleteById(anyInt())
    }
}
