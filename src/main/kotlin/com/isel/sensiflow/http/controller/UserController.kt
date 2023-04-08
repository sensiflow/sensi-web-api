package com.isel.sensiflow.http.controller

import com.isel.sensiflow.Constants.User.AUTH_COOKIE_NAME
import com.isel.sensiflow.Constants.User.SESSION_EXPIRATION_TIME
import com.isel.sensiflow.http.entities.input.UserLoginInput
import com.isel.sensiflow.http.entities.input.UserRegisterInput
import com.isel.sensiflow.http.entities.output.IDOutput
import com.isel.sensiflow.http.entities.output.UserOutput
import com.isel.sensiflow.http.entities.output.toIDOutput
import com.isel.sensiflow.http.pipeline.authentication.Authentication
import com.isel.sensiflow.http.utils.createAuthCookie
import com.isel.sensiflow.http.utils.removeCookie
import com.isel.sensiflow.services.UserID
import com.isel.sensiflow.services.UserService
import com.isel.sensiflow.services.dto.toOutput
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(RequestPaths.Users.USERS)
class UserController(private val userService: UserService) {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun registerHandler(
        @RequestBody @Valid userInput: UserRegisterInput,
        response: HttpServletResponse
    ): IDOutput {
        val authInfo = userService.createUser(userInput)

        val authCookie = createAuthCookie(authInfo.token, SESSION_EXPIRATION_TIME)

        response.addCookie(authCookie)
        return authInfo.userID.toIDOutput()
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(RequestPaths.Users.GET_USER)
    fun getUserHandler(
        @PathVariable userID: UserID
    ): UserOutput =
        userService.getUser(userID).toOutput()

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(RequestPaths.Users.LOGIN)
    fun loginHandler(
        @RequestBody @Valid userInput: UserLoginInput,
        response: HttpServletResponse
    ): IDOutput {
        val authInfo = userService.authenticateUser(userInput)

        val authCookie = createAuthCookie(authInfo.token, authInfo.timeUntilExpire)

        response.addCookie(authCookie)

        return authInfo.userID.toIDOutput()
    }

    @Authentication
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(RequestPaths.Users.LOGOUT)
    fun logoutHandler(
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        val authCookie = request.cookies?.find { it.name == AUTH_COOKIE_NAME }
        requireNotNull(authCookie) { "Required not to be null because @Authentication" }

        userService.invalidateSessionToken(authCookie.value)

        response.removeCookie(authCookie)
    }
}
