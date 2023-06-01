package com.isel.sensiflow.http.controller

import com.isel.sensiflow.Constants.User.AUTH_COOKIE_NAME
import com.isel.sensiflow.http.controller.RequestPaths.Users
import com.isel.sensiflow.http.entities.input.UserLoginInput
import com.isel.sensiflow.http.entities.input.UserRegisterInput
import com.isel.sensiflow.http.entities.input.UserUpdateInput
import com.isel.sensiflow.http.entities.output.AuthOutput
import com.isel.sensiflow.http.entities.output.IDOutput
import com.isel.sensiflow.http.entities.output.UserOutput
import com.isel.sensiflow.http.entities.output.toIDOutput
import com.isel.sensiflow.http.pipeline.authentication.Authentication
import com.isel.sensiflow.http.utils.createAuthCookie
import com.isel.sensiflow.http.utils.removeCookie
import com.isel.sensiflow.services.Role.ADMIN
import com.isel.sensiflow.services.Role.MODERATOR
import com.isel.sensiflow.services.Role.USER
import com.isel.sensiflow.services.UserID
import com.isel.sensiflow.services.UserService
import com.isel.sensiflow.services.dto.PageableDTO
import com.isel.sensiflow.services.dto.input.UserRoleInput
import com.isel.sensiflow.services.dto.output.PageDTO
import com.isel.sensiflow.services.dto.toOutput
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(Users.USERS)
class UserController(private val userService: UserService) {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @Authentication(authorization = MODERATOR)
    fun createUser(
        @RequestBody @Valid userInput: UserRegisterInput,
        response: HttpServletResponse
    ): IDOutput =
        userService.createUser(userInput).toIDOutput()

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(Users.GET_USER)
    fun getUser(
        @PathVariable id: UserID
    ): UserOutput =
        userService.getUser(id).toOutput()

    @ResponseStatus(HttpStatus.OK)
    @GetMapping()
    @Authentication(authorization = USER)
    fun getUsers(
        @RequestParam page: Int?,
        @RequestParam pageSize: Int?
    ): PageDTO<UserOutput> =
        userService.getUsers(PageableDTO(page, pageSize))

    @Authentication(authorization = USER)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(Users.GET_USER)
    fun updateUser(
        @PathVariable id: UserID,
        userID: UserID,
        @RequestBody @Valid userInput: UserUpdateInput,
    ) {
        userService.updateUser(id, userID, userInput)
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(Users.LOGIN)
    fun login(
        @RequestBody @Valid userInput: UserLoginInput,
        response: HttpServletResponse
    ): AuthOutput {
        val authInfo = userService.authenticateUser(userInput)

        val authCookie = createAuthCookie(authInfo.token, authInfo.timeUntilExpire)

        response.addCookie(authCookie)

        return AuthOutput(authInfo.userID,authInfo.timeUntilExpire)
    }//TODO: change documentation

    @Authentication(authorization = USER)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(Users.LOGOUT)
    fun logout(
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        val authCookie = request.cookies?.find { it.name == AUTH_COOKIE_NAME }
        requireNotNull(authCookie) { "Required not to be null because @Authentication" }

        userService.invalidateSessionToken(authCookie.value)

        response.removeCookie(authCookie)
    }

    @Authentication(authorization = ADMIN)
    @PutMapping(Users.ROLE)
    fun updateRole(
        @PathVariable id: UserID,
        @RequestBody @Valid inputDTO: UserRoleInput,
    ): ResponseEntity<Unit> {
        userService.updateRole(id, inputDTO)

        return ResponseEntity
            .noContent()
            .build()
    }
}
