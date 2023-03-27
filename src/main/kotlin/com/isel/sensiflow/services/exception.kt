package com.isel.sensiflow.services

import com.isel.sensiflow.Constants

class UserNotFoundException(id: Int) : Exception(Constants.Error.USER_NOT_FOUND.format(id))
class OwnerMismatchException(message: String) : Exception(message)
class DeviceNotFoundException(id: Int) : Exception(Constants.Error.DEVICE_NOT_FOUND.format(id))
class DeviceGroupNotFoundException(id: Int) : Exception(Constants.Error.DEVICE_GROUP_NOT_FOUND.format(id))
class ProcessedStreamNotFoundException(id: Int) : Exception(Constants.Error.PROCESSED_STREAM_NOT_FOUND.format(id))
class InputConstraintViolationException(message: String) : Exception(message)
