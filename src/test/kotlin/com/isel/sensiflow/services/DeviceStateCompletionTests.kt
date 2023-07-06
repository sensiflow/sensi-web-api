package com.isel.sensiflow.services

import com.isel.sensiflow.amqp.instanceController.MessageSender
import com.isel.sensiflow.amqp.message.output.InstanceMessage
import com.isel.sensiflow.model.entities.Device
import com.isel.sensiflow.model.entities.DeviceProcessingState
import com.isel.sensiflow.model.repository.DeviceRepository
import com.isel.sensiflow.model.repository.MetricRepository
import com.isel.sensiflow.services.beans.DeviceProcessingStateService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.util.Optional

@RunWith(MockitoJUnitRunner::class)
class DeviceStateCompletionTests {

    @InjectMocks
    private lateinit var deviceProcessingStateService: DeviceProcessingStateService

    @Mock
    private lateinit var deviceRepository: DeviceRepository

    @Mock
    private lateinit var metricRepository: MetricRepository

    @Mock
    private lateinit var instanceControllerMessageSender: MessageSender

    @BeforeEach
    fun initMocks() {
        MockitoAnnotations.openMocks(this)
    }

    private val fakeDevice = Device(
        id = 1,
        name = "Device 1",
        streamURL = "rtsp://example.com/device1/stream/buckbuckbunny",
        description = "Device 1 description",
        processedStreamURL = null
    )

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

        Mockito.`when`(deviceRepository.findById(existingDevice.id)).thenReturn(Optional.of(existingDevice))

        deviceProcessingStateService.startUpdateProcessingState(fakeDevice.id, to.name)

        Mockito.verify(deviceRepository, Mockito.times(1)).findById(existingDevice.id)
    }

    @Test
    fun `simple update state for a device`() {
        val device = fakeDevice.id
        Mockito.`when`(deviceRepository.findById(device)).thenReturn(Optional.of(fakeDevice))

        updateStateTransition(DeviceProcessingState.INACTIVE, DeviceProcessingState.ACTIVE)

        Mockito.verify(deviceRepository, Mockito.times(1)).save(kAny(Device::class.java))
        Mockito.verify(instanceControllerMessageSender, Mockito.times(1))
            .sendMessage(kAny(InstanceMessage::class.java))
    }

    @Test
    fun `update state for a device that does not exist`() {
        val nonExistantDevice = fakeDevice.id + 1
        Mockito.`when`(deviceRepository.findById(nonExistantDevice)).thenReturn(Optional.empty())

        assertThrows<DeviceNotFoundException> {
            deviceProcessingStateService.startUpdateProcessingState(nonExistantDevice, DeviceProcessingState.ACTIVE.name)
        }

        Mockito.verify(deviceRepository, Mockito.times(1)).findById(nonExistantDevice)
        Mockito.verify(deviceRepository, Mockito.times(0)).save(kAny(Device::class.java))
        Mockito.verify(instanceControllerMessageSender, Mockito.times(0))
            .sendMessage(kAny(InstanceMessage::class.java))
    }

    @Test
    fun `update a state with a processing state that doesn't exist fails`() {
        val nonExistantState = "nonExistantState"

        assertThrows<InvalidProcessingStateException> {
            deviceProcessingStateService.startUpdateProcessingState(fakeDevice.id, nonExistantState)
        }

        Mockito.verify(deviceRepository, Mockito.times(0)).findById(fakeDevice.id)
        Mockito.verify(deviceRepository, Mockito.times(0)).save(kAny(Device::class.java))
        Mockito.verify(instanceControllerMessageSender, Mockito.times(0))
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
            Mockito.verify(deviceRepository, Mockito.times(expectedSaveCalls)).save(kAny(Device::class.java))
            Mockito.verify(instanceControllerMessageSender, Mockito.times(expectedSaveCalls))
                .sendMessage(kAny(InstanceMessage::class.java))

            // Reset mock
            Mockito.reset(deviceRepository)
            Mockito.reset(instanceControllerMessageSender)
        }

        invalidTransitions.forEach { (fromState, toState) ->

            assertThrows<InvalidProcessingStateTransitionException> {
                updateStateTransition(fromState, toState)
            }

            Mockito.verify(deviceRepository, Mockito.times(0)).save(kAny(Device::class.java))
            Mockito.verify(instanceControllerMessageSender, Mockito.times(0))
                .sendMessage(kAny(InstanceMessage::class.java))

            // Reset not needed because mocks are never called
        }
    }

    @Test
    fun `updating a device's processing state when this is already updating fails`() {

        val storedDevice = Device(
            id = 1,
            name = "teste",
            description = "teste",
            streamURL = "https://streamUrl.com/assert",
            pendingUpdate = true,
            processedStreamURL = null
        )

        Mockito.`when`(deviceRepository.findById(1)).thenReturn(Optional.of(storedDevice))

        assertThrows<DeviceAlreadyUpdatingException> {
            deviceProcessingStateService.startUpdateProcessingState(1, "ACTIVE")
        }

        Mockito.verify(deviceRepository, Mockito.times(0)).save(kAny(Device::class.java))
        Mockito.verify(instanceControllerMessageSender, Mockito.times(0))
            .sendMessage(kAny(InstanceMessage::class.java))
    }

    // Device Processing State Update Completion

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
        Mockito.`when`(deviceRepository.findById(deviceID)).thenReturn(Optional.of(device))

        deviceProcessingStateService.completeUpdateState(deviceID, DeviceProcessingState.ACTIVE)

        Mockito.verify(deviceRepository, Mockito.times(1)).save(kAny(Device::class.java))
    }

    @Test
    fun `update device's state after receiving a message from the rabbit queue when the device does not exist throws DeviceNotFoundException`() {
        val deviceID = fakeDevice.id
        Mockito.`when`(deviceRepository.findById(deviceID)).thenReturn(Optional.empty())

        assertThrows<DeviceNotFoundException> {
            deviceProcessingStateService.completeUpdateState(deviceID, DeviceProcessingState.ACTIVE)
        }

        Mockito.verify(deviceRepository, Mockito.times(0)).save(kAny(Device::class.java))
    }

    @Test
    fun `update device's state after receiving a message from the rabbit queue when the device's state is not pending throws ServiceInternalException`() {
        val deviceID = fakeDevice.id
        Mockito.`when`(deviceRepository.findById(deviceID)).thenReturn(Optional.of(fakeDevice))

        assertThrows<ServiceInternalException> {
            deviceProcessingStateService.completeUpdateState(deviceID, DeviceProcessingState.INACTIVE)
        }

        Mockito.verify(deviceRepository, Mockito.times(0)).save(kAny(Device::class.java))
    }
}
