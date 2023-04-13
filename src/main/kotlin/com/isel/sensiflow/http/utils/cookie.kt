package com.isel.sensiflow.http.utils

import com.isel.sensiflow.Constants
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse

/**
 * Sets the max age of the cookie in milliseconds
 */
fun Cookie.maxAge(maxAge: Long): Cookie {
    this.maxAge = (maxAge / 1000).toInt()
    return this
}

/**
 * Sets the path of the cookie
 */
fun Cookie.path(path: String): Cookie {
    this.path = path
    return this
}

/**
 * Sets the httpOnly property of the cookie
 */
fun Cookie.httpOnly(httpOnly: Boolean): Cookie {
    this.isHttpOnly = httpOnly
    return this
}

/**
 * Creates an authentication cookie with the given token and time until expiration
 * this cookie is set to be httpOnly and its path is set to /api
 * @param token The token to be set as the cookie value
 * @param timeUntilExpire The time until the cookie expires in milliseconds
 */
fun createAuthCookie(token: String, timeUntilExpire: Long): Cookie {
    return Cookie(Constants.User.AUTH_COOKIE_NAME, token)
        .apply {
            path(Constants.CONTEXT_PATH)
            maxAge(timeUntilExpire)
            httpOnly(true)
        }
}

/**
 * Removes a cookie by setting its value to empty and its maxAge to 0 (immediately expires)
 * and adds it to the response
 */
fun HttpServletResponse.removeCookie(cookie: Cookie) {
    cookie.value = ""
    cookie.maxAge = 0
    addCookie(cookie)
}
