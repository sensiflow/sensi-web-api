package com.isel.sensiflow

object Constants {

    object Device {
        const val NAME_MAX_LENGTH = 20
        const val DESCRIPTION_MAX_LENGTH = 100
        const val STREAM_URL_MAX_LENGTH = 200
        const val NAME_MIN_LENGTH = 3
    }

    object User {
        const val SESSION_EXPIRATION_TIME = 1000L * 60 * 60 * 24 // 1 day
        const val AUTH_COOKIE_NAME = "SessionAuth"
    }

    object Security {
        const val SALT_LENGTH = 32
        const val DIGEST_ALGORITHM = "SHA-512"
        const val A_DAY_IN_MILLIS = 1000L * 60 * 60 * 24
    }

    object InputValidation {
        const val EMAIL_MAX_LENGTH = 100
        const val NAME_MIN_LENGTH = 3
        const val NAME_MAX_LENGTH = 20
        const val PASSWORD_MIN_SIZE = 3
        const val PASSWORD_MAX_SIZE = 20
        const val PASSWORD_REGEX = """^(?=.*[a-z])(?=.*[A-Z])(?=.*[*.!@$%^&(){}\[\]:;<>,.?/~_+-=|]).{$PASSWORD_MIN_SIZE,$PASSWORD_MAX_SIZE}$"""
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
        const val PROCESSED_STREAM_NOT_FOUND = "Processed stream with id %d not found"
        const val DEVICE_GROUP_NOT_FOUND = "Device group with id %d not found"
    }
}
