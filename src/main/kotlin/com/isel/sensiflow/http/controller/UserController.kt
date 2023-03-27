package com.isel.sensiflow.http.controller

import com.isel.sensiflow.Constants.User.AUTH_COOKIE_NAME
import com.isel.sensiflow.Constants.User.SESSION_EXPIRATION_TIME
import com.isel.sensiflow.http.entities.input.UserLoginInput
import com.isel.sensiflow.http.entities.input.UserRegisterInput
import com.isel.sensiflow.http.entities.output.UserIDOutput
import com.isel.sensiflow.http.entities.output.UserOutput
import com.isel.sensiflow.http.pipeline.authentication.Authentication
import com.isel.sensiflow.http.utils.httpOnly
import com.isel.sensiflow.http.utils.maxAge
import com.isel.sensiflow.http.utils.path
import com.isel.sensiflow.http.utils.removeCookie
import com.isel.sensiflow.services.UserID
import com.isel.sensiflow.services.UserService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(private val userService: UserService) {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(RequestPaths.Users.REGISTER)
    fun registerHandler(@RequestBody @Valid userInput: UserRegisterInput, response: HttpServletResponse): UserIDOutput {
        val authInfo = userService.createUser(userInput)

        val authCookie = Cookie(AUTH_COOKIE_NAME, authInfo.token)
            .apply {
                path("/")
                maxAge(SESSION_EXPIRATION_TIME)
                httpOnly(true)
            }
        response.addCookie(authCookie)
        return UserIDOutput(authInfo.userID)
    }

    @Authentication
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(RequestPaths.Users.GET_USER)
    fun getUserHandler(@PathVariable userID: UserID): UserOutput {
        return userService.getUser(userID)
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(RequestPaths.Users.LOGIN)
    fun loginHandler(@RequestBody userInput: UserLoginInput, response: HttpServletResponse): UserIDOutput { // todo: test expiring
        val authInfo = userService.authenticateUser(userInput)

        val authCookie = Cookie(AUTH_COOKIE_NAME, authInfo.token)
            .apply {
                path("/")
                maxAge(authInfo.timeUntilExpire)
                httpOnly(true)
            }
        response.addCookie(authCookie)

        return UserIDOutput(authInfo.userID)
    }

    @Authentication
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(RequestPaths.Users.LOGOUT)
    fun logoutHandler(request: HttpServletRequest, response: HttpServletResponse) { // TODO: se nao houver cookie, nao fazer nada ???
        val authCookie = request.cookies?.find { it.name == AUTH_COOKIE_NAME }
        requireNotNull(authCookie) { "Required not to be null because @Authentication" }

        userService.invalidateSessionToken(authCookie.value)

        response.removeCookie(authCookie)
    }
}

// use HTTPOnly on cookie  // explain why there is no need of hmac
