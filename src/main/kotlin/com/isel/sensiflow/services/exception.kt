package com.isel.sensiflow.services

import com.isel.sensiflow.Constants
import com.isel.sensiflow.model.dao.DeviceProcessingState

sealed class ServiceException(message: String) : Exception(message)

class UserNotFoundException(id: Int) : ServiceException(Constants.Error.USER_NOT_FOUND.format(id))
class OwnerMismatchException(message: String) : ServiceException(message)
class DeviceNotFoundException(id: Int) : ServiceException(Constants.Error.DEVICE_NOT_FOUND.format(id))
class InvalidProcessingStateException(state: String) : ServiceException(
    Constants.Error.PROCESSING_STATE_INVALID.format(
        state,
        DeviceProcessingState.valuesRepresentation()
    )
)
class InvalidProcessingStateTransitionException(from: DeviceProcessingState, to: DeviceProcessingState) :
    ServiceException("Invalid transition from $from to $to")
class DeviceGroupNotFoundException(id: Int) : ServiceException(Constants.Error.DEVICE_GROUP_NOT_FOUND.format(id))
class ProcessedStreamNotFoundException(id: Int) : ServiceException(Constants.Error.PROCESSED_STREAM_NOT_FOUND.format(id))