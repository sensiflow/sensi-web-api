package com.isel.sensiflow.http.utils

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
 * Removes a cookie by setting its value to empty and its maxAge to 0 (immediately expires)
 * and adds it to the response
 */
fun HttpServletResponse.removeCookie(cookie: Cookie) {
    cookie.value = ""
    cookie.maxAge = 0
    addCookie(cookie)
}
