package com.isel.sensiflow.amqp

import com.isel.sensiflow.model.dao.DeviceProcessingState

/**
 * Represents a possible action to be performed on a device.
 */
enum class Action {
    START,
    STOP,
    REMOVE,
    PAUSE;

    companion object {

        fun fromString(value: String): Action? {
            return try {
                Action.valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}

/**
 * Returns the action that should be performed on the device to transition to the receiver state.
 */
val DeviceProcessingState.action: Action get() = when (this) {
    DeviceProcessingState.ACTIVE -> Action.START
    DeviceProcessingState.PAUSED -> Action.PAUSE
    DeviceProcessingState.INACTIVE -> Action.STOP
}

/**
 * Returns the action that should be performed on the device to transition to the receiver state.
 */
val Action.state: DeviceProcessingState get() = when (this) {
    Action.START -> DeviceProcessingState.ACTIVE
    Action.PAUSE -> DeviceProcessingState.PAUSED
    Action.STOP -> DeviceProcessingState.INACTIVE
    Action.REMOVE -> DeviceProcessingState.INACTIVE
}
