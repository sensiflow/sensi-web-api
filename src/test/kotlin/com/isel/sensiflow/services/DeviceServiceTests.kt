package com.isel.sensiflow.services

import com.isel.sensiflow.amqp.InstanceMessage
import com.isel.sensiflow.amqp.instanceController.MessageSender
import com.isel.sensiflow.model.entities.Device
import com.isel.sensiflow.model.entities.DeviceGroup
import com.isel.sensiflow.model.entities.DeviceProcessingState
import com.isel.sensiflow.model.entities.Metric
import com.isel.sensiflow.model.entities.MetricID
import com.isel.sensiflow.model.entities.User
import com.isel.sensiflow.model.entities.UserRole
import com.isel.sensiflow.model.repository.DeviceGroupRepository
import com.isel.sensiflow.model.repository.DeviceRepository
import com.isel.sensiflow.model.repository.MetricRepository
import com.isel.sensiflow.model.repository.UserRepository
import com.isel.sensiflow.services.dto.PageableDTO
import com.isel.sensiflow.services.dto.input.DeviceInputDTO
import com.isel.sensiflow.services.dto.input.DeviceUpdateDTO
import com.isel.sensiflow.services.dto.input.DevicesGroupCreateDTO
import com.isel.sensiflow.services.dto.output.DeviceProcessingStateOutput
import com.isel.sensiflow.services.dto.output.DeviceSimpleOutputDTO
import com.isel.sensiflow.services.dto.output.toDeviceOutputDTO
import com.isel.sensiflow.services.dto.output.toMetricOutputDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyList
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
import org.springframework.data.domain.Pageable
import java.sql.Timestamp
import java.util.Optional

@RunWith(MockitoJUnitRunner::class)
class DeviceServiceTests {

    @InjectMocks
    private lateinit var deviceService: DeviceService

    @InjectMocks
    private lateinit var deviceGroupService: DeviceGroupService

    @Mock
    private lateinit var instanceControllerMessageSender: MessageSender

    @Mock
    private lateinit var deviceRepository: DeviceRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var metricRepository: MetricRepository

    @Mock
    private lateinit var deviceGroupRepository: DeviceGroupRepository

    @BeforeEach
    fun initMocks() {
        MockitoAnnotations.openMocks(this)
    }

    private val ADMINRole = UserRole(
        id = 1,
        role = Role.ADMIN.name
    )

    private val fakeUser = User(
        id = 1,
        firstName = "John",
        lastName = "Doe",
        role = ADMINRole,
        passwordHash = "hash",
        passwordSalt = "salt",
        email = "johnDoe@email.com"
    )

    private val fakeDevice = Device(
        id = 1,
        name = "Device 1",
        streamURL = "rtsp://example.com/device1/stream/buckbuckbunny",
        description = "Device 1 description",
        processedStreamURL = null
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
        device = fakeDevice
    )

    @Test
    fun `creating a device with an existing user`() {
        val userId = 1

        `when`(deviceRepository.save(kAny(Device::class.java))).thenReturn(fakeDevice)

        val createdDevice = deviceService.createDevice(fakeDeviceInput, userId)

        assertEquals(fakeDevice.toDeviceOutputDTO(expanded = false), createdDevice)

        verify(deviceRepository, times(1)).save(kAny(Device::class.java))
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
            processedStreamURL = null
        )

        `when`(deviceRepository.save(kAny(Device::class.java))).thenReturn(device)

        val createdDevice = deviceService.createDevice(deviceInputNoDescription, userId)

        assertEquals(device.toDeviceOutputDTO(expanded = false), createdDevice)

        verify(deviceRepository, times(1)).save(kAny(Device::class.java))
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
            processingState = DeviceProcessingStateOutput.INACTIVE,
            deviceGroupsID = emptyList(),
            processedStreamURL = null
        )
        assertEquals(expected, retrievedDevice)
        verify(deviceRepository, times(1)).findById(deviceId)
    }

    @Test
    fun `simple get all devices expanded`() {
        val pageableDTO = PageableDTO(page = 1, size = 10)
        val devices = listOf(
            Device(
                id = 1,
                name = "Device 1",
                streamURL = "https://example.com/device1/stream",
                description = "Device 1 description",
                processedStreamURL = null
            ),
            Device(
                id = 2,
                name = "Device 2",
                streamURL = "https://example.com/device2/stream",
                description = "Device 2 description",
                processedStreamURL = null
            )
        )
        val page = PageImpl(devices, PageRequest.of(pageableDTO.page, pageableDTO.size), devices.size.toLong())

        `when`(deviceRepository.findAll(PageRequest.of(pageableDTO.page, pageableDTO.size)))
            .thenReturn(page)

        val retrievedDevices = deviceService.getAllDevices(pageableDTO, expanded = true)

        val expected = devices.map { it.toDeviceOutputDTO(expanded = true) }

        assertEquals(expected, retrievedDevices.items)
        verify(deviceRepository, times(1))
            .findAll(PageRequest.of(pageableDTO.page, pageableDTO.size))
    }

    @Test
    fun `simple device update`() {
        // Arrange
        val deviceId = 1
        val existingDevice = fakeDevice

        `when`(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice))

        val updatedDevice = Device(
            id = 1,
            name = "Updated Test Device",
            description = "This is an updated test device.",
            streamURL = "rtsp://example.com/device1/stream",
            processedStreamURL = null
        )
        val deviceDto = DeviceUpdateDTO(
            name = updatedDevice.name,
            streamURL = updatedDevice.streamURL,
            description = updatedDevice.description
        )

        `when`(deviceRepository.save(kAny(Device::class.java))).thenReturn(updatedDevice)

        // Act
        deviceService.updateDevice(deviceId, deviceDto)

        // Assert
        verify(deviceRepository, times(1)).findById(deviceId)
        verify(deviceRepository, times(1)).save(kAny(Device::class.java))
        verify(instanceControllerMessageSender, times(0))
            .sendMessage(kAny(InstanceMessage::class.java))
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
            deviceService.updateDevice(deviceId, deviceDto) // uid Irrelevant
        }

        // Assert
        verify(deviceRepository, times(1)).findById(deviceId)
        verify(deviceRepository, times(0)).save(kAny(Device::class.java))
        verify(instanceControllerMessageSender, times(0))
            .sendMessage(kAny(InstanceMessage::class.java))
    }

    @Test
    fun `update the streamUrl of an active device stops it`() {
        // Arrange
        val deviceId = 1
        val storedDevice = Device(
            id = deviceId,
            name = "Device",
            streamURL = "rtsp://myurl.com/test",
            description = "What a description!",
            processingState = DeviceProcessingState.ACTIVE,
            processedStreamURL = null
        )
        `when`(deviceRepository.findById(deviceId)).thenReturn(Optional.of(storedDevice))

        val deviceDto = DeviceUpdateDTO(
            name = "Updated Test Device",
            streamURL = "https://example.com/device1/stream",
            description = "description"
        )

        deviceService.updateDevice(deviceId, deviceDto)

        // Assert
        verify(deviceRepository, times(1)).findById(deviceId)
        verify(deviceRepository, times(1)).save(kAny(Device::class.java))
        verify(instanceControllerMessageSender, times(1))
            .sendMessage(kAny(InstanceMessage::class.java))
    }

    @Test
    fun `update the streamUrl of a paused device stops it`() {
        // Arrange
        val deviceId = 1
        val storedDevice = Device(
            id = deviceId,
            name = "Device",
            streamURL = "rtsp://myurl.com/test",
            description = "What a description!",
            processingState = DeviceProcessingState.PAUSED,
            processedStreamURL = null
        )
        `when`(deviceRepository.findById(deviceId)).thenReturn(Optional.of(storedDevice))

        val deviceDto = DeviceUpdateDTO(
            name = "Updated Test Device",
            streamURL = "https://example.com/device1/stream",
            description = "description"
        )

        deviceService.updateDevice(deviceId, deviceDto)

        // Assert
        verify(deviceRepository, times(1)).findById(deviceId)
        verify(deviceRepository, times(1)).save(kAny(Device::class.java))
        verify(instanceControllerMessageSender, times(1))
            .sendMessage(kAny(InstanceMessage::class.java))
    }

    @Test
    fun `simple delete device`() {
        // Arrange
        val deviceId = 1
        val deviceIds = listOf(deviceId)
        val devicesToDelete = listOf(fakeDevice)
        `when`(deviceRepository.findAllById(deviceIds)).thenReturn(devicesToDelete)
        doNothing().`when`(deviceRepository).deleteById(deviceId)

        // Act
        deviceService.deleteDevices(deviceIds)

        // Assert
        verify(deviceRepository, times(1)).findAllById(deviceIds)
        verify(deviceRepository, times(1)).flagForDeletion(devicesToDelete)
    }

    @Test
    fun `delete device when device does not exist`() {
        // Arrange
        val nonExistingDevice = 1
        val deviceIds = listOf(nonExistingDevice)
        `when`(deviceRepository.findAllById(deviceIds)).thenReturn(emptyList())
        // Act
        assertThrows<DeviceNotFoundException> {
            deviceService.deleteDevices(deviceIds)
        }

        // Assert
        verify(deviceRepository, times(0)).flagForDeletion(anyList())
    }

    @Test
    fun `delete a device that is on a group`() {
        // Arrange
        val groupDTO = DevicesGroupCreateDTO(
            name = "Test group",
        )

        val fakeDeviceGroup = DeviceGroup(
            name = groupDTO.name,
            description = null
        )

        `when`(deviceGroupRepository.save(kAny(DeviceGroup::class.java))).thenReturn(fakeDeviceGroup)
        `when`(deviceRepository.findAllById(anyList())).thenReturn(listOf(fakeDevice))
        `when`(deviceRepository.findById(anyInt())).thenReturn(Optional.of(fakeDevice))
        `when`(deviceGroupRepository.findById(anyInt())).thenReturn(Optional.of(fakeDeviceGroup))

        // Act
        val result = deviceGroupService.createDevicesGroup(groupDTO, listOf(fakeDevice.id))

        // Assert
        assertEquals(fakeDeviceGroup, result)

        verify(deviceGroupRepository, times(1)).save(kAny(DeviceGroup::class.java))
        val devicesToDelete = listOf(fakeDevice)
        val deviceIds = listOf(fakeDevice.id)
        deviceService.deleteDevices(deviceIds)

        verify(deviceRepository, times(1)).flagForDeletion(devicesToDelete)
        verify(deviceGroupRepository, times(1)).saveAll(anyList())
        verify(metricRepository, times(1)).deleteAllByDeviceIn(devicesToDelete)
    }

    /* State transition tests */

    /**
     * Helper function to test valid and invalid state transitions. This function just performs the transition
     * and verifies the things that should happen when a transition is made with valid processing states.
     */
    private fun updateStateTransition(from: DeviceProcessingState, to: DeviceProcessingState) {

        val existingDevice = Device(
            id = fakeDevice.id,
            name = fakeDevice.name,
            streamURL = fakeDevice.streamURL,
            description = fakeDevice.description,
            processingState = from,
            processedStreamURL = null
        )

        `when`(deviceRepository.findById(existingDevice.id)).thenReturn(Optional.of(existingDevice))

        deviceService.startUpdateProcessingState(fakeDevice.id, to.name)

        verify(deviceRepository, times(1)).findById(existingDevice.id)
    }

    @Test
    fun `simple update state for a device`() {
        val device = fakeDevice.id
        `when`(deviceRepository.findById(device)).thenReturn(Optional.of(fakeDevice))

        updateStateTransition(DeviceProcessingState.INACTIVE, DeviceProcessingState.ACTIVE)

        verify(deviceRepository, times(1)).save(kAny(Device::class.java))
        verify(instanceControllerMessageSender, times(1))
            .sendMessage(kAny(InstanceMessage::class.java))
    }

    @Test
    fun `update state for a device that does not exist`() {
        val nonExistantDevice = fakeDevice.id + 1
        `when`(deviceRepository.findById(nonExistantDevice)).thenReturn(Optional.empty())

        assertThrows<DeviceNotFoundException> {
            deviceService.startUpdateProcessingState(nonExistantDevice, DeviceProcessingState.ACTIVE.name)
        }

        verify(deviceRepository, times(1)).findById(nonExistantDevice)
        verify(deviceRepository, times(0)).save(kAny(Device::class.java))
        verify(instanceControllerMessageSender, times(0))
            .sendMessage(kAny(InstanceMessage::class.java))
    }

    @Test
    fun `update a state with a processing state that doesn't exist fails`() {
        val nonExistantState = "nonExistantState"

        assertThrows<InvalidProcessingStateException> {
            deviceService.startUpdateProcessingState(fakeDevice.id, nonExistantState)
        }

        verify(deviceRepository, times(0)).findById(fakeDevice.id)
        verify(deviceRepository, times(0)).save(kAny(Device::class.java))
        verify(instanceControllerMessageSender, times(0))
            .sendMessage(kAny(InstanceMessage::class.java))
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
            from.isValidTransition(to) || from == to
        }

        validTransitions.forEach { (fromState, toState) ->

            val isSameStateTransition = fromState == toState

            updateStateTransition(fromState, toState)

            val expectedSaveCalls = if (isSameStateTransition) 0 else 1

            // Save is only called when the state has actually changed
            verify(deviceRepository, times(expectedSaveCalls)).save(kAny(Device::class.java))
            verify(instanceControllerMessageSender, times(expectedSaveCalls))
                .sendMessage(kAny(InstanceMessage::class.java))

            // Reset mock
            reset(deviceRepository)
            reset(instanceControllerMessageSender)
        }

        invalidTransitions.forEach { (fromState, toState) ->

            assertThrows<InvalidProcessingStateTransitionException> {
                updateStateTransition(fromState, toState)
            }

            verify(deviceRepository, times(0)).save(kAny(Device::class.java))
            verify(instanceControllerMessageSender, times(0))
                .sendMessage(kAny(InstanceMessage::class.java))

            // Reset not needed because mocks are never called
        }
    }

    @Test
    fun `updating a device's processing state when this is already updating fails`(){

        val storedDevice = Device(
            id=1,
            name="teste",
            description="teste",
            streamURL = "https://streamUrl.com/assert",
            pendingUpdate = true,
            processedStreamURL = null
        )

        `when`(deviceRepository.findById(1)).thenReturn(Optional.of(storedDevice))

        assertThrows<DeviceAlreadyUpdatingException> {
            deviceService.startUpdateProcessingState(1, "ACTIVE")
        }

        verify(deviceRepository, times(0)).save(kAny(Device::class.java))
        verify(instanceControllerMessageSender, times(0))
            .sendMessage(kAny(InstanceMessage::class.java))
    }

    @Test
    fun `get device stats successfully`() {
        // Arrange
        val deviceId = 1
        val pageableDTO = PageableDTO(page = 1, size = 10)

        val expectedPageItems = listOf<Metric>(
            Metric(
                id = fakeDeviceStats.id,
                endTime = fakeDeviceStats.endTime,
                peopleCount = fakeDeviceStats.peopleCount,
                device = fakeDevice
            )
        )

        val page = PageImpl(
            expectedPageItems,
            PageRequest.of(pageableDTO.page, pageableDTO.size),
            expectedPageItems.size.toLong()
        )

        `when`(deviceRepository.findById(deviceId)).thenReturn(Optional.of(fakeDevice))
        `when`(metricRepository.findAllByDeviceId(fakeDevice.id, PageRequest.of(pageableDTO.page, pageableDTO.size)))
            .thenReturn(page)

        val expected = expectedPageItems.map { it.toMetricOutputDTO() }

        // Act

        val retrievedStats = deviceService.getDeviceStats(pageableDTO, deviceId)

        // Assert
        assertEquals(expected, retrievedStats.items)
        verify(metricRepository, times(1))
            .findAllByDeviceId(fakeDevice.id, PageRequest.of(pageableDTO.page, pageableDTO.size))
    }

    @Test
    fun `get device stats when device does not exist`() {
        // Arrange
        val deviceId = 1
        val pageableDTO = PageableDTO(page = 1, size = 10)

        `when`(deviceRepository.findById(deviceId)).thenReturn(Optional.empty())
        `when`(metricRepository.findAllByDeviceId(kAny(Int::class.java), kAny(Pageable::class.java)))
            .thenReturn(null)

        // Act
        assertThrows<DeviceNotFoundException> {
            deviceService.getDeviceStats(pageableDTO, deviceId)
        }

        // Assert
        verify(metricRepository, times(0))
            .findAllByDeviceId(kAny(Int::class.java), kAny(Pageable::class.java))
    }

    @Test
    fun `update device's state successfully after receiving a message from the rabbit queue`() {
        val deviceID = fakeDevice.id + 1
        val device = Device(
            id = deviceID,
            name = fakeDevice.name,
            streamURL = fakeDevice.streamURL,
            description = fakeDevice.description,
            processingState = DeviceProcessingState.INACTIVE,
            pendingUpdate = true,
            processedStreamURL = null
        )
        `when`(deviceRepository.findById(deviceID)).thenReturn(Optional.of(device))

        deviceService.completeUpdateState(deviceID, DeviceProcessingState.ACTIVE)

        verify(deviceRepository, times(1)).save(kAny(Device::class.java))
    }

    @Test
    fun `update device's state after receiving a message from the rabbit queue when the device does not exist throws DeviceNotFoundException`() {
        val deviceID = fakeDevice.id
        `when`(deviceRepository.findById(deviceID)).thenReturn(Optional.empty())

        assertThrows<DeviceNotFoundException> {
            deviceService.completeUpdateState(deviceID, DeviceProcessingState.ACTIVE)
        }

        verify(deviceRepository, times(0)).save(kAny(Device::class.java))
    }

    @Test
    fun `update device's state after receiving a message from the rabbit queue when the device's state is not pending throws ServiceInternalException`() {
        val deviceID = fakeDevice.id
        `when`(deviceRepository.findById(deviceID)).thenReturn(Optional.of(fakeDevice))

        assertThrows<ServiceInternalException> {
            deviceService.completeUpdateState(deviceID, DeviceProcessingState.INACTIVE)
        }

        verify(deviceRepository, times(0)).save(kAny(Device::class.java))
    }

    @Test
    fun `delete a device after receiving a message from the rabbit queue successfully`() {
        val deviceID = fakeDevice.id
        `when`(deviceRepository.findById(deviceID)).thenReturn(Optional.of(fakeDevice))
        fakeDevice.scheduledForDeletion = true
        deviceService.completeDeviceDeletion(deviceID)

        verify(deviceRepository, times(1)).delete(fakeDevice)
    }

    @Test
    fun `delete a device after receiving a message from the rabbit queue when the device does not exist throws DeviceNotFoundException`() {
        val deviceID = fakeDevice.id
        `when`(deviceRepository.findById(deviceID)).thenReturn(Optional.empty())

        assertThrows<DeviceNotFoundException> {
            deviceService.completeDeviceDeletion(deviceID)
        }

        verify(deviceRepository, times(0)).delete(fakeDevice)
    }

    @Test
    fun `delete a device after receiving a message from the rabbit queue when the device is not scheduled for deletion throws ServiceInternalException`() {
        val deviceID = fakeDevice.id
        `when`(deviceRepository.findById(deviceID)).thenReturn(Optional.of(fakeDevice))

        assertThrows<ServiceInternalException> {
            deviceService.completeDeviceDeletion(deviceID)
        }

        verify(deviceRepository, times(0)).delete(fakeDevice)
    }
}
