package com.isel.sensiflow

object Constants {

    const val API_VERSION = "v1"
    const val CONTEXT_PATH = "/api/$API_VERSION"

    object Device {
        const val PEOPLE_COUNT_RETRIEVAL_DELAY: Long = 1000 // 1 second
        const val DEVICE_PROCESSING_STATE_RETRIEVAL_DELAY: Long = 3000 // 1 second
        const val NAME_MAX_LENGTH = 20
        const val DESCRIPTION_MAX_LENGTH = 100
        const val STREAM_URL_MAX_LENGTH = 200
        const val NAME_MIN_LENGTH = 3
        const val STREAM_URL_REGEX = "^((http|https|rtsp|rtmp)://.)[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)$"
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

    object Roles {
        const val ROLE_NAME_MIN_LENGTH = 4
        const val ROLE_NAME_MAX_LENGTH = 9
    }

    object InputValidation {
        const val EMAIL_MAX_LENGTH = 100
        const val NAME_MIN_LENGTH = 3
        const val NAME_MAX_LENGTH = 20
        const val PASSWORD_MIN_SIZE = 5
        const val PASSWORD_MAX_SIZE = 20
        const val PASSWORD_REGEX = """^(?=.*[a-z])(?=.*[A-Z])(?=.*[*.!@$%^&(){}\[\]:;<>,.?/~_+-=|]).{$PASSWORD_MIN_SIZE,$PASSWORD_MAX_SIZE}$"""
    }

    object Pagination {
        const val DEFAULT_PAGE = 0
        const val DEFAULT_PAGE_SIZE = 10
        const val MAX_PAGE_SIZE = 50
    }

    object Error {
        const val PROCESSING_STATE_INVALID = "Invalid processing state: '%s' please use one of the following: %s"
        const val EMAIL_ALREADY_EXISTS = "Email %s already exists"
        const val USER_NOT_FOUND = "User with id %d not found"
        const val DEVICE_NOT_FOUND = "Device with id %d not found"
        const val ROLE_NOT_FOUND = "Role with name %s not found"
        const val EMAIL_NOT_FOUND = "User with email %s not found"
        const val DEVICE_NAME_EMPTY = "Name cannot be empty"
        const val DEVICE_STREAM_URL_EMPTY = "Stream URL cannot be empty"
        const val USER_ROLE_NAME_INVALID_LENGTH = "Role name must be between ${Roles.ROLE_NAME_MIN_LENGTH} and ${Roles.ROLE_NAME_MAX_LENGTH} characters"
        const val DEVICE_NAME_INVALID_LENGTH = "Name must be between 1 and ${Device.NAME_MAX_LENGTH} characters"
        const val DEVICE_DESCRIPTION_INVALID_LENGTH =
            "Description must be between 1 and ${Device.DESCRIPTION_MAX_LENGTH} characters"
        const val DEVICE_STREAM_URL_INVALID_LENGTH =
            "Stream URL must be between 1 and ${Device.STREAM_URL_MAX_LENGTH} characters"
        const val PASSWORD_REGEX_MISMATCH = "Password must contain at least one uppercase letter, one lowercase letter, one digit and one special character"
        const val PASSWORD_INVALID_LENGTH = "Password must be between ${InputValidation.PASSWORD_MIN_SIZE} and ${InputValidation.PASSWORD_MAX_SIZE} characters"
        const val EMAIL_INVALID_LENGTH = "Email must be between 1 and ${InputValidation.EMAIL_MAX_LENGTH} characters"
        const val PASSWORD_EMPTY = "Password cannot be empty"
        const val EMAIL_EMPTY = "Email cannot be empty"
        const val NAME_EMPTY = "Name cannot be empty"
        const val EMAIL_INVALID_FORMAT = "Email is not valid"
        const val PROCESSED_STREAM_NOT_FOUND = "Processed stream with id %d not found"
        const val DEVICE_GROUP_NOT_FOUND = "Device group with id %d not found"
        const val DEVICE_STATE_REQUIRED = "Processing state is required"
        const val DEVICE_STREAM_URL_INVALID = "Stream URL does not match the required format"
        const val DEVICE_ID_MUST_BE_POSITVE = "Device id must be positive"
        const val DEVICE_ALREADY_UPDATING = "A request to update this device has already been made, please wait for it to finish"
    }

    object Problem {

        object URI {
            private const val BASE_URI = "https://sensiflow.github.io/main/api/errors/general/"

            const val DEVICE_NOT_FOUND = "$BASE_URI#device-not-found"
            const val USER_NOT_FOUND = "$BASE_URI#user-not-found"
            const val ROLE_NOT_FOUND = "$BASE_URI#role-not-found"
            const val PROCESSED_STREAM_NOT_FOUND = "$BASE_URI#processed-stream-not-found"
            const val DEVICE_GROUP_NOT_FOUND = "$BASE_URI#device-group-not-found"
            const val EMAIL_NOT_FOUND = "$BASE_URI#email-not-found"
            const val UNAUTHORIZED = "$BASE_URI#unauthorized"
            const val UNAUTHENTICATED = "$BASE_URI#unauthenticated"
            const val INVALID_CREDENTIALS = "$BASE_URI#invalid-credentials"
            const val INVALID_PROCESSING_STATE = "$BASE_URI#invalid-processing-state"
            const val INVALID_TOKEN = "$BASE_URI#invalid-token"
            const val INVALID_PROCESSING_STATE_TRANSITION = "$BASE_URI#invalid-processing-state-transition"
            const val EMAIL_ALREADY_EXISTS = "$BASE_URI#email-already-exists"
            const val URI_HANDLER_NOT_FOUND = "$BASE_URI#handler-not-found"
            const val URI_VALIDATION_ERROR = "$BASE_URI#invalid-parameter"
            const val INVALID_JSON_BODY = "$BASE_URI#invalid-json-structure"
            const val URI_REQUIRED_PATH_PARAMETER_MISSING = "$BASE_URI#required-uri-parameter-missing"
            const val URI_METHOD_NOT_ALLOWED = "$BASE_URI#method-not-allowed"
            const val SERVICE_INTERNAL = "$BASE_URI#internal-server-error"
            const val DEVICE_ALREADY_UPDATING = "$BASE_URI#device-already-updating"
        }

        object Title {
            const val NOT_FOUND = "The requested resource was not found"
            const val INVALID_CREDENTIALS = "The provided credentials are invalid"
            const val UNAUTHORIZED = "You are not authorized to perform this action"
            const val UNAUTHENTICATED = "You must be authenticated to perform this action"
            const val INVALID_PROCESSING_STATE = "The provided Processing State is invalid"
            const val INVALID_PROCESSING_STATE_TRANSITION = "An invalid Processing State Transition was requested"
            const val ALREADY_EXISTS = "The requested resource already exists"
            const val INVALID_TOKEN = "The provided token is invalid"
            const val HANDLER_NOT_FOUND = "The requested uri does not have a handler associated with it"
            const val METHOD_NOT_ALLOWED = "The requested method is not allowed for the requested uri"
            const val VALIDATION_ERROR = "The provided data is invalid"
            const val REQUIRED_PARAMETER_MISSING = "A required parameter is missing"
            const val INVALID_JSON_BODY = "The provided JSON body has an invalid structure"
            const val INTERNAL_ERROR = "An internal error occurred"
            const val DEVICE_ALREADY_UPDATING = "A device update is ongoing"
        }
    }

    object Cache {
        const val DEVICE = "device"
        const val DEVICE_LIST = "deviceList"
    }
}
