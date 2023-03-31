package com.isel.sensiflow.services

import com.isel.sensiflow.model.dao.Device
import com.isel.sensiflow.model.dao.DeviceProcessingState
import com.isel.sensiflow.model.dao.Email
import com.isel.sensiflow.model.dao.Metric
import com.isel.sensiflow.model.dao.MetricID
import com.isel.sensiflow.model.dao.User
import com.isel.sensiflow.model.dao.addEmail
import com.isel.sensiflow.model.repository.DeviceRepository
import com.isel.sensiflow.model.repository.MetricRepository
import com.isel.sensiflow.model.repository.UserRepository
import com.isel.sensiflow.services.dto.PaginationInfo
import com.isel.sensiflow.services.dto.input.DeviceInputDTO
import com.isel.sensiflow.services.dto.input.DeviceUpdateDTO
import com.isel.sensiflow.services.dto.output.DeviceSimpleOutputDTO
import com.isel.sensiflow.services.dto.output.toDTO
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
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.sql.Timestamp
import java.util.Optional

@RunWith(MockitoJUnitRunner::class)
class DeviceServiceTests {

    @InjectMocks
    private lateinit var deviceService: DeviceService

    @Mock
    private lateinit var deviceRepository: DeviceRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var metricRepository: MetricRepository

    @BeforeEach
    fun initMocks() {
        fakeUser.devices.add(fakeDevice)
        fakeUser.email = fakeUserEmail

        MockitoAnnotations.openMocks(this)
    }

    private val fakeUser = User(
        id = 1,
        firstName = "John",
        lastName = "Doe",
        passwordHash = "hash",
        passwordSalt = "salt"
    )
    private val fakeUserEmail = Email(
        user = fakeUser,
        email = "johnDoe@email.com"
    )

    private val fakeDevice = Device(
        id = 1,
        name = "Device 1",
        streamURL = "https://example.com/device1/stream",
        description = "Device 1 description",
        user = fakeUser
    )

    private val fakeDeviceInput = DeviceInputDTO(
        name = fakeDevice.name,
        streamURL = fakeDevice.streamURL,
        description = fakeDevice.description
    )

    private val fakeDeviceStats = Metric(
        id = MetricID(1, Timestamp.valueOf("2021-01-01 00:00:00")),
        endTime = Timestamp.valueOf("2021-01-01 00:00:01"),
        peopleCount = 1,
        deviceID = fakeDevice
    )

    @Test
    fun `creating a device with an existing user`() {
        val userId = 1

        `when`(userRepository.findById(userId)).thenReturn(Optional.of(fakeUser))
        `when`(deviceRepository.save(any(Device::class.java))).thenReturn(fakeDevice)

        val createdDevice = deviceService.createDevice(fakeDeviceInput, userId)

        assertEquals(fakeDevice.toDTO(expanded = false), createdDevice)

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
            streamURL = fakeDevice.streamURL,
            description = "",
            user = fakeUser
        )

        `when`(userRepository.findById(userId)).thenReturn(Optional.of(fakeUser))
        `when`(deviceRepository.save(any(Device::class.java))).thenReturn(device)

        val createdDevice = deviceService.createDevice(deviceInputNoDescription, userId)

        assertEquals(device.toDTO(expanded = false), createdDevice)

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
            streamURL = fakeDevice.streamURL,
            description = fakeDevice.description,
            userID = fakeDevice.user.id,
            processingState = "INACTIVE"
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
                streamURL = "https://example.com/device1/stream",
                description = "Device 1 description",
                user = User(
                    id = 1,
                    firstName = "John",
                    lastName = "Doe",
                    passwordHash = "hash",
                    passwordSalt = "salt"
                ).addEmail(fakeUserEmail)
            ),
            Device(
                id = 2,
                name = "Device 2",
                streamURL = "https://example.com/device2/stream",
                description = "Device 2 description",
                user = User(
                    id = 2,
                    firstName = "Jane",
                    lastName = "Doe",
                    passwordHash = "hash",
                    passwordSalt = "salt"
                ).addEmail(
                    Email(
                        user = fakeUser,
                        email = "janeDoe@email.com"
                    )
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
            streamURL = "https://example.com/device1/stream",
            user = fakeDevice.user
        )
        val deviceDto = DeviceUpdateDTO(
            name = updatedDevice.name,
            streamURL = updatedDevice.streamURL,
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
            streamURL = "https://example.com/device1/stream",
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
            streamURL = "https://example.com/device1/stream",
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

    /* State transition tests */

    /**
     * Helper function to test valid and invalid state transitions. This function just performs the transition
     * and verifies the things that should happen when a transition is made with valid provided states.
     */
    private fun updateStateTransition(from: DeviceProcessingState, to: DeviceProcessingState) {

        val existingDevice = Device(
            id = fakeDevice.id,
            name = fakeDevice.name,
            streamURL = fakeDevice.streamURL,
            description = fakeDevice.description,
            processingState = from,
            user = fakeDevice.user
        )

        `when`(deviceRepository.findById(existingDevice.id)).thenReturn(Optional.of(existingDevice))

        deviceService.updateProcessingState(fakeDevice.id, to.name, fakeDevice.user.id)

        verify(deviceRepository, times(1)).findById(existingDevice.id)
    }

    @Test
    fun `simple update state for a device`() {
        val device = fakeDevice.id
        `when`(deviceRepository.findById(device)).thenReturn(Optional.of(fakeDevice))

        updateStateTransition(DeviceProcessingState.INACTIVE, DeviceProcessingState.ACTIVE)

        verify(deviceRepository, times(1)).save(any(Device::class.java))
    }

    @Test
    fun `update state with a user that does not own the device`() {

        val notOwnerID = fakeDevice.user.id + 1
        `when`(deviceRepository.findById(fakeDevice.id)).thenReturn(Optional.of(fakeDevice))

        assertThrows<OwnerMismatchException> {
            deviceService.updateProcessingState(fakeDevice.id, DeviceProcessingState.ACTIVE.name, notOwnerID)
        }

        verify(deviceRepository, times(0)).save(any(Device::class.java))
    }

    @Test
    fun `update state for a device that does not exist`() {
        val nonExistantDevice = fakeDevice.id + 1
        `when`(deviceRepository.findById(nonExistantDevice)).thenReturn(Optional.empty())

        assertThrows<DeviceNotFoundException> {
            deviceService.updateProcessingState(nonExistantDevice, DeviceProcessingState.ACTIVE.name, fakeUser.id)
        }

        verify(deviceRepository, times(1)).findById(nonExistantDevice)
        verify(deviceRepository, times(0)).save(any(Device::class.java))
    }

    @Test
    fun `update a state with a state that doesn't exist fails`() {
        val nonExistantState = "nonExistantState"

        assertThrows<InvalidProcessingStateException> {
            deviceService.updateProcessingState(fakeDevice.id, nonExistantState, fakeUser.id)
        }

        verify(deviceRepository, times(0)).findById(fakeDevice.id)
        verify(deviceRepository, times(0)).save(any(Device::class.java))
    }

    @Test
    fun `update a state with a null state fails`() {
        val nonExistantState = null

        assertThrows<InvalidProcessingStateException> {
            deviceService.updateProcessingState(fakeDevice.id, nonExistantState, fakeUser.id)
        }

        verify(deviceRepository, times(0)).findById(fakeDevice.id)
        verify(deviceRepository, times(0)).save(any(Device::class.java))
    }

    @Test
    fun `valid transitions test`() {

        val possibleStates = DeviceProcessingState.values().toList()

        // Every single enum value list combination
        val allPossibleTransitions = possibleStates.map { state ->
            state to possibleStates
        }.flatMap { (state, possibleStates) ->
            possibleStates.map {
                state to it
            }
        }

        val (validTransitions, invalidTransitions) = allPossibleTransitions.partition { (from, to) ->
            from.isValidTransition(to)
        }

        validTransitions.forEach { (fromState, toState) ->

            updateStateTransition(fromState, toState)

            verify(deviceRepository, times(1)).save(any(Device::class.java))

            // Reset mock
            reset(deviceRepository)
        }

        invalidTransitions.forEach { (fromState, toState) ->

            assertThrows<InvalidProcessingStateTransitionException> {
                updateStateTransition(fromState, toState)
            }

            verify(deviceRepository, times(0)).save(any(Device::class.java))

            // Reset mock
            reset(deviceRepository)
        }
    }

    @Test
    fun `get device stats successfully`() {
        // Arrange
        val deviceId = 1
        val paginationInfo = PaginationInfo(page = 1, size = 10)

        val expectedPageItems = listOf<Metric>(
            Metric(
                id = fakeDeviceStats.id,
                endTime = fakeDeviceStats.endTime,
                peopleCount = fakeDeviceStats.peopleCount,
                deviceID = fakeDevice
            )
        )

        val page = PageImpl(
            expectedPageItems,
            PageRequest.of(paginationInfo.page, paginationInfo.size),
            expectedPageItems.size.toLong()
        )

        `when`(deviceRepository.findById(deviceId)).thenReturn(Optional.of(fakeDevice))
        `when`(metricRepository.findAllByDeviceID(deviceId, PageRequest.of(paginationInfo.page, paginationInfo.size)))
            .thenReturn(page)

        val expected = expectedPageItems.map { it.toDTO() }

        // Act
        val retrievedStats = deviceService.getDeviceStats(paginationInfo, deviceId, fakeUser.id)

        // Assert
        assertEquals(expected, retrievedStats.items)
        verify(metricRepository, times(1))
            .findAllByDeviceID(deviceId, PageRequest.of(paginationInfo.page, paginationInfo.size))
    }

    @Test
    fun `get device stats when device does not exist`() {
        // Arrange
        val deviceId = 1
        val paginationInfo = PaginationInfo(page = 1, size = 10)

        `when`(deviceRepository.findById(deviceId)).thenReturn(Optional.empty())

        // Act
        assertThrows<DeviceNotFoundException> {
            deviceService.getDeviceStats(paginationInfo, deviceId, fakeUser.id)
        }

        // Assert
        verify(metricRepository, times(0))
            .findAllByDeviceID(deviceId, PageRequest.of(paginationInfo.page, paginationInfo.size))
    }

    @Test
    fun `get device stats when user is not the owner`() {
        // Arrange
        val deviceId = 1
        val paginationInfo = PaginationInfo(page = 1, size = 10)

        `when`(deviceRepository.findById(deviceId)).thenReturn(Optional.of(fakeDevice))

        // Act
        assertThrows<OwnerMismatchException> {
            deviceService.getDeviceStats(paginationInfo, deviceId, fakeUser.id + 1)
        }

        // Assert
        verify(metricRepository, times(0))
            .findAllByDeviceID(deviceId, PageRequest.of(paginationInfo.page, paginationInfo.size))
    }
}
