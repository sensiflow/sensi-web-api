package com.isel.sensiflow.model.dao

import com.isel.sensiflow.amqp.Action
import com.isel.sensiflow.model.dao.DeviceProcessingState.ACTIVE
import com.isel.sensiflow.model.dao.DeviceProcessingState.INACTIVE
import com.isel.sensiflow.model.dao.DeviceProcessingState.PAUSED
import com.isel.sensiflow.services.InvalidProcessingStateTransitionException

enum class DeviceProcessingState {
    ACTIVE,
    PAUSED,
    INACTIVE;

    companion object {
        fun valuesRepresentation(): String {
            return enumValues<DeviceProcessingState>().joinToString()
        }

        fun fromString(value: String): DeviceProcessingState? {
            return try {
                DeviceProcessingState.valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    fun isValidTransition(newState: DeviceProcessingState): Boolean {
        return when (this) {
            ACTIVE -> newState == PAUSED || newState == INACTIVE
            PAUSED -> newState == ACTIVE || newState == INACTIVE
            INACTIVE -> newState == ACTIVE
        }
    }
}

/**
 * Returns the action that should be performed on the device to transition from the current state to the new state.
 * @throws [InvalidProcessingStateTransitionException] if the transition is invalid.
 */
fun DeviceProcessingState.transitionToAction(newProcessingState: DeviceProcessingState): Action {
    val invalidStateException = InvalidProcessingStateTransitionException(
        from = this,
        to = newProcessingState
    )
    return when (this) {
        ACTIVE -> when (newProcessingState) {
            PAUSED -> Action.PAUSE
            INACTIVE -> Action.STOP
            else -> throw invalidStateException
        }
        PAUSED -> when (newProcessingState) {
            ACTIVE -> Action.START
            INACTIVE -> Action.STOP
            else -> throw invalidStateException
        }
        INACTIVE -> when (newProcessingState) {
            ACTIVE -> Action.START
            else -> throw invalidStateException
        }
    }
}
