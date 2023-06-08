package com.isel.sensiflow.http.pipeline.authentication

import com.isel.sensiflow.services.Role
import com.isel.sensiflow.services.UserID

/**
 * Used on Rest Controller handlers to indicate that it requires authentication.
 *
 * The handler annotated with [Authentication] can specify a parameter
 * that must be named userID and of type [UserID] to receive the user id.
 *
 * This userID will be injected in the controller by the [AuthenticationInterceptor] and [UserIDArgumentResolver].
 *
 * @param authorization The authorization level required to access the handler.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class Authentication(val authorization: Role = Role.USER)
