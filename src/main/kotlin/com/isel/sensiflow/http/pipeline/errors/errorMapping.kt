package com.isel.sensiflow.http.pipeline.errors

import com.isel.sensiflow.Constants
import com.isel.sensiflow.services.AlreadyExistsException
import com.isel.sensiflow.services.DeviceGroupNotFoundException
import com.isel.sensiflow.services.DeviceNotFoundException
import com.isel.sensiflow.services.EmailAlreadyExistsException
import com.isel.sensiflow.services.EmailNotFoundException
import com.isel.sensiflow.services.InvalidCredentialsException
import com.isel.sensiflow.services.InvalidProcessingStateException
import com.isel.sensiflow.services.InvalidProcessingStateTransitionException
import com.isel.sensiflow.services.NotFoundException
import com.isel.sensiflow.services.OwnerMismatchException
import com.isel.sensiflow.services.ProcessedStreamNotFoundException
import com.isel.sensiflow.services.ServiceException
import com.isel.sensiflow.services.ServiceInternalException
import com.isel.sensiflow.services.UnauthenticatedException
import com.isel.sensiflow.services.UnauthorizedException
import com.isel.sensiflow.services.UserNotFoundException
import org.springframework.http.HttpStatus
import java.net.URI

/**
 * @returns the associated HTTP status code as [HttpStatus] for the given [ServiceException]
 */
val ServiceException.httpCode: HttpStatus
    get() = when (this) {
        is NotFoundException -> HttpStatus.NOT_FOUND

        is InvalidCredentialsException -> HttpStatus.BAD_REQUEST

        is UnauthenticatedException -> HttpStatus.UNAUTHORIZED

        is UnauthorizedException -> HttpStatus.FORBIDDEN
        is OwnerMismatchException -> HttpStatus.FORBIDDEN

        is InvalidProcessingStateException -> HttpStatus.BAD_REQUEST
        is InvalidProcessingStateTransitionException -> HttpStatus.BAD_REQUEST
        is EmailAlreadyExistsException -> HttpStatus.CONFLICT

        is AlreadyExistsException -> HttpStatus.CONFLICT
        is ServiceInternalException -> HttpStatus.INTERNAL_SERVER_ERROR

        else -> {
            HttpStatus.INTERNAL_SERVER_ERROR
        }
    }

/**
 * @returns the associated error [URI] for the given [ServiceException]
 */
val ServiceException.errorURI: URI
    get() = when (this) { // TODO: change this urls to our documentation urls
        is DeviceGroupNotFoundException -> URI.create(Constants.Problem.URI.DEVICE_GROUP_NOT_FOUND)
        is UserNotFoundException -> URI.create(Constants.Problem.URI.USER_NOT_FOUND)
        is EmailNotFoundException -> URI.create(Constants.Problem.URI.EMAIL_NOT_FOUND)
        is ProcessedStreamNotFoundException -> URI.create(Constants.Problem.URI.PROCESSED_STREAM_NOT_FOUND)
        is DeviceNotFoundException -> URI.create(Constants.Problem.URI.DEVICE_NOT_FOUND)
        is InvalidCredentialsException -> URI.create(Constants.Problem.URI.INVALID_CREDENTIALS)
        is UnauthorizedException -> URI.create(Constants.Problem.URI.UNAUTHORIZED)
        is UnauthenticatedException -> URI.create(Constants.Problem.URI.UNAUTHENTICATED)
        is OwnerMismatchException -> URI.create(Constants.Problem.URI.OWNER_MISMATCH)
        is InvalidProcessingStateException -> URI.create(Constants.Problem.URI.INVALID_PROCESSING_STATE)
        is InvalidProcessingStateTransitionException -> URI.create(Constants.Problem.URI.INVALID_PROCESSING_STATE_TRANSITION)
        is EmailAlreadyExistsException -> URI.create(Constants.Problem.URI.EMAIL_ALREADY_EXISTS)
        is ServiceInternalException -> URI.create(Constants.Problem.URI.SERVICE_INTERNAL)
    }

/**
 * @returns the associated error title for the given [ServiceException]
 */
val ServiceException.title: String
    get() = when (this) {
        is NotFoundException -> Constants.Problem.Title.NOT_FOUND
        is InvalidCredentialsException -> Constants.Problem.Title.INVALID_CREDENTIALS
        is UnauthorizedException -> Constants.Problem.Title.UNAUTHORIZED
        is UnauthenticatedException -> Constants.Problem.Title.UNAUTHENTICATED
        is OwnerMismatchException -> Constants.Problem.Title.OWNER_MISMATCH
        is InvalidProcessingStateException -> Constants.Problem.Title.INVALID_PROCESSING_STATE
        is InvalidProcessingStateTransitionException -> Constants.Problem.Title.INVALID_PROCESSING_STATE_TRANSITION
        is AlreadyExistsException -> Constants.Problem.Title.ALREADY_EXISTS
        is ServiceInternalException -> Constants.Problem.Title.INTERNAL_ERROR
    }