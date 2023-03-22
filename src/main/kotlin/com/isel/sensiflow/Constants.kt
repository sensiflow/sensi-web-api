package com.isel.sensiflow

object Constants {

    object Device {
        const val NAME_MAX_LENGTH = 20
        const val DESCRIPTION_MAX_LENGTH = 100
        const val STREAM_URL_MAX_LENGTH = 200
    }

    object Pagination {
        const val DEFAULT_PAGE = 1
        const val DEFAULT_PAGE_SIZE = 10
        const val MAX_PAGE_SIZE = 50
    }

    object Error {
        const val USER_NOT_FOUND = "User with id %d not found"
        const val DEVICE_NOT_FOUND = "Device with id %d not found"
        const val DEVICE_OWNER_MISMATCH = "Device with id %d does not belong to user with id %d"
        const val DEVICE_NAME_EMPTY = "Name cannot be empty"
        const val DEVICE_STREAM_URL_EMPTY = "Stream URL cannot be empty"
        const val DEVICE_NAME_INVALID_LENGTH = "Name must be between 1 and ${Device.NAME_MAX_LENGTH} characters"
        const val DEVICE_DESCRIPTION_INVALID_LENGTH =
            "Description must be between 1 and ${Device.DESCRIPTION_MAX_LENGTH} characters"
        const val DEVICE_STREAM_URL_INVALID_LENGTH =
            "Stream URL must be between 1 and ${Device.STREAM_URL_MAX_LENGTH} characters"
    }
}
