package com.isel.sensiflow.http.pipeline.authentication

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

/**
 * Intercepts the request and checks if the handler requires authentication.
 *
 * If it does then it will check if the request has a valid Authorization Cookie,
 * parse it and inject the user id into the handler arguments.
 */
@Component
class AuthenticationInterceptor(
    private val cookieAuthorizationProcessor: CookieAuthorizationProcessor
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod && handler.hasMethodAnnotation(Authentication::class.java)) {
            val cookies = request.cookies ?: null
            val userID = cookieAuthorizationProcessor.process(cookies)

            UserIDArgumentResolver.addUserIDTo(userID, request)
            return true
        }
        return true
    }
}
