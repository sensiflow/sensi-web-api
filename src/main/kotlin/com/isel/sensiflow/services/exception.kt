package com.isel.sensiflow.services

import com.isel.sensiflow.Constants

class UserNotFoundException(id: Int) : Exception(Constants.Error.USER_NOT_FOUND.format(id))
class OwnerMismatchException(message: String) : Exception(message)
class DeviceNotFoundException(id: Int) : Exception(Constants.Error.DEVICE_NOT_FOUND.format(id))
class InputConstraintViolationException(message: String) : Exception(message)
