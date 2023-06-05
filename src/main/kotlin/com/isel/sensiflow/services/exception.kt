package com.isel.sensiflow.services

import com.isel.sensiflow.Constants
import com.isel.sensiflow.model.entities.DeviceProcessingState

/**
 * Base class for all service exceptions.
 */
sealed class ServiceException(message: String) : Exception(message)

/**
 * Indicates that the resource was not found.
 */
sealed class NotFoundException(message: String) : ServiceException(message)

class UserNotFoundException(id: Int) : NotFoundException(Constants.Error.USER_NOT_FOUND.format(id))
class EmailNotFoundException(email: String) : NotFoundException(Constants.Error.EMAIL_NOT_FOUND.format(email))
class DeviceGroupNotFoundException(id: Int) : NotFoundException(Constants.Error.DEVICE_GROUP_NOT_FOUND.format(id))
class ProcessedStreamNotFoundException(id: Int) : NotFoundException(Constants.Error.PROCESSED_STREAM_NOT_FOUND.format(id))
class DeviceNotFoundException(id: Int) : NotFoundException(Constants.Error.DEVICE_NOT_FOUND.format(id))
class RoleNotFoundException(name: String) : NotFoundException(Constants.Error.ROLE_NOT_FOUND.format(name))

/**
 * Indicates that the resource already exists.
 */
sealed class ResourceConflictException(message: String) : ServiceException(message)

/**
 * Indicates that the given email already exists.
 */
class EmailAlreadyExistsException(email: String) :
    ResourceConflictException(Constants.Error.EMAIL_ALREADY_EXISTS.format(email))

/**
 * Indicates that the given processing state is invalid.
 */
class InvalidProcessingStateException(state: String) : ServiceException(
    Constants.Error.PROCESSING_STATE_INVALID.format(
        state,
        DeviceProcessingState.valuesRepresentation()
    )
)

class DeviceAlreadyUpdatingException(id: Int) : ResourceConflictException(
    Constants.Error.DEVICE_ALREADY_UPDATING.format(id)
)

/**
 * Indicates that the given parameter is invalid.
 */
class InvalidParameterException(message: String) : ServiceException(message)

/**
 * Indicates that the given state transition is not valid.
 */
class InvalidProcessingStateTransitionException(from: DeviceProcessingState, to: DeviceProcessingState) :
    ServiceException("Invalid transition from $from to $to")

/**
 * Indicates that the user is not authenticated.
 * Thrown when the user is not authenticated.
 * The user can authenticate to be able to perform the operation.
 */
class UnauthenticatedException(message: String) : ServiceException(message)

/**
 * Indicates that the user is not authorized to perform the operation. (note that this happens when the user is authenticated)
 * The user may require additional permissions or roles to perform the operation.
 * Similar to UnauthenticatedException, but this exception is thrown when the user is authenticated.
 */
class ActionForbiddenException(message: String) : ServiceException(message)

/**
 * Indicates that the user provided an invalid token.
 */
class InvalidTokenException(message: String) : ServiceException(message)

/**
 * Indicates that the user provided invalid credentials.
 */
class InvalidCredentialsException(message: String) : ServiceException(message)

/**
 * Indicates that an unexpected internal error occurred.
 */
class ServiceInternalException(message: String) : ServiceException(message)
