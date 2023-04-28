package com.isel.sensiflow.amqp

import com.isel.sensiflow.model.dao.DeviceProcessingState

/**
 * Represents a possible action to be performed on a device.
 */
enum class Action {
    START,
    STOP,
    PAUSE
}

/**
 * Returns the action that should be performed on the device to transition to the receiver state.
 */
val DeviceProcessingState.action: Action get() = when (this) {
    DeviceProcessingState.ACTIVE -> Action.START
    DeviceProcessingState.PAUSED -> Action.PAUSE
    DeviceProcessingState.INACTIVE -> Action.STOP
}
