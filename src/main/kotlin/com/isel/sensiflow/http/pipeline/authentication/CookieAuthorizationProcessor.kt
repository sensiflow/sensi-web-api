package com.isel.sensiflow.http.pipeline.authentication

import com.isel.sensiflow.Constants.User.AUTH_COOKIE_NAME
import com.isel.sensiflow.services.UserID
import com.isel.sensiflow.services.UserService
import jakarta.servlet.http.Cookie
import org.springframework.stereotype.Component

/**
 * Represents the processor for the Authorization Cookie used in the authentication process
 */
@Component
class CookieAuthorizationProcessor(
    val userServices: UserService
) {
    fun process(cookies: Array<Cookie>?): UserID {
        val authCookie = cookies?.find { it.name == AUTH_COOKIE_NAME } ?: throw Exception() // unauthenticated

        return userServices.validateSessionToken(authCookie.value)
    }
}
