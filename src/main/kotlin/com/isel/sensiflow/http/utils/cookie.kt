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
 * Represents the SameSite attribute of a cookie
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie/SameSite">SameSite</a>
 * @property STRICT
 * Means that the browser sends the cookie only for same-site requests, that is, requests originating from the same site that set the cookie.
 * If a request originates from a different domain or scheme (even with the same domain), no cookies with the SameSite=Strict attribute are sent.
 *
 * @property LAX
 * Means that the cookie is not sent on cross-site requests, such as on requests to load images or frames,
 * but is sent when a user is navigating to the origin site from an external site (for example, when following a link).
 * This is the default behavior if the SameSite attribute is not specified.
 *
 * @property NONE
 * means that the browser sends the cookie with both cross-site and same-site requests.
 * The Secure attribute must also be set when setting this value, like so SameSite=None; Secure.
 */
enum class SameSite(val value: String) {
    STRICT("Strict"),
    LAX("Lax"),
    NONE("None")
}

fun Cookie.sameSite(sameSite: SameSite): Cookie {
    this.setAttribute("SameSite", sameSite.value)
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
            path(Constants.API_PATH)
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
