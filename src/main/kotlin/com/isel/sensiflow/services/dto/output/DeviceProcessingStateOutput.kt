package com.isel.sensiflow.services.dto.output

import com.isel.sensiflow.model.dao.DeviceProcessingState

enum class DeviceProcessingStateOutput {
    PENDING,
    ACTIVE,
    PAUSED,
    INACTIVE;
}

fun DeviceProcessingState.toDeviceProcessingStateOutput(): DeviceProcessingStateOutput = when (this) {
    DeviceProcessingState.ACTIVE -> DeviceProcessingStateOutput.ACTIVE
    DeviceProcessingState.PAUSED -> DeviceProcessingStateOutput.PAUSED
    DeviceProcessingState.INACTIVE -> DeviceProcessingStateOutput.INACTIVE
}
