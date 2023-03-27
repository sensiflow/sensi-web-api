package com.isel.sensiflow.services.dto

import com.isel.sensiflow.Constants.User.SESSION_EXPIRATION_TIME
import com.isel.sensiflow.services.UserID

/**
 * Represents the information obtained after a successful authentication.
 * @param token Session token of the user
 * @param userID ID of the user
 * @param timeUntilExpire Time until the session token expires in milliseconds, used to set the cookie expiration time if needed
 */
class AuthInformationDTO(
    val token: String,
    val userID: UserID,
    val timeUntilExpire: Long = SESSION_EXPIRATION_TIME
)
