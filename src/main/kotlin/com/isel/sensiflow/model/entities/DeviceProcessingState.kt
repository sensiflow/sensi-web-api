package com.isel.sensiflow.model.entities

import com.isel.sensiflow.amqp.ProcessingAction
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

fun getNextProcessingState(oldState: DeviceProcessingState, newState: DeviceProcessingState): ProcessingAction {
    return when (oldState) {
        DeviceProcessingState.ACTIVE -> when (newState) {
            DeviceProcessingState.PAUSED -> ProcessingAction.PAUSE
            DeviceProcessingState.INACTIVE -> ProcessingAction.STOP
            else -> {
                throw InvalidProcessingStateTransitionException(oldState, newState)
            }
        }
        DeviceProcessingState.PAUSED -> when (newState) {
            DeviceProcessingState.ACTIVE -> ProcessingAction.RESUME
            DeviceProcessingState.INACTIVE -> ProcessingAction.STOP
            else -> {
                throw InvalidProcessingStateTransitionException(oldState, newState)
            }
        }
        DeviceProcessingState.INACTIVE -> when (newState) {
            DeviceProcessingState.ACTIVE -> ProcessingAction.START
            DeviceProcessingState.PAUSED -> ProcessingAction.PAUSE
            else -> {
                throw InvalidProcessingStateTransitionException(oldState, newState)
            }
        }
    }
}
