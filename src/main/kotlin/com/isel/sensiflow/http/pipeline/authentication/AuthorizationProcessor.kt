package com.isel.sensiflow.http.pipeline.authentication

import com.isel.sensiflow.services.ActionForbiddenException
import com.isel.sensiflow.services.Role
import com.isel.sensiflow.services.UserID
import com.isel.sensiflow.services.UserService
import com.isel.sensiflow.services.hasAccessTo
import org.springframework.stereotype.Component

@Component
class AuthorizationProcessor(
    val userService: UserService
) {

    /**
     * Checks if the user has the required role to access the resource.
     * @param userID the user id.
     * @param role the required role.
     * @throws ActionForbiddenException if the user does not have the required role.
     */
    fun process(userID: UserID, role: Role) {
        val user = userService.getUser(userID)
        if (!user.role.hasAccessTo(role)) throw ActionForbiddenException("User $userID is not authorized to access this resource")
    }
}
