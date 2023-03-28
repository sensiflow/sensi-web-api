package com.isel.sensiflow.model.dao

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
