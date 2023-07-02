package com.isel.sensiflow.amqp

import com.isel.sensiflow.model.entities.DeviceProcessingState

/**
 * Represents a possible action to be performed on a device.
 */
enum class ProcessingAction {
    START,
    STOP,
    REMOVE,
    PAUSE;

    companion object {

        fun fromString(value: String): ProcessingAction? {
            return try {
                ProcessingAction.valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}

/**
 * Returns the action that should be performed on the device to transition to the receiver state.
 */
val DeviceProcessingState.action: ProcessingAction get() = when (this) {
    DeviceProcessingState.ACTIVE -> ProcessingAction.START
    DeviceProcessingState.PAUSED -> ProcessingAction.PAUSE
    DeviceProcessingState.INACTIVE -> ProcessingAction.STOP
}

/**
 * Returns the action that should be performed on the device to transition to the receiver state.
 */
val ProcessingAction.state: DeviceProcessingState get() = when (this) {
    ProcessingAction.START -> DeviceProcessingState.ACTIVE
    ProcessingAction.PAUSE -> DeviceProcessingState.PAUSED
    ProcessingAction.STOP -> DeviceProcessingState.INACTIVE
    ProcessingAction.REMOVE -> DeviceProcessingState.INACTIVE
}
