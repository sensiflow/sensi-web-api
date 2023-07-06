package com.isel.sensiflow.http.pipeline.errors

import com.isel.sensiflow.Constants
import com.isel.sensiflow.Constants.Problem.Title.HANDLER_NOT_FOUND
import com.isel.sensiflow.Constants.Problem.Title.METHOD_NOT_ALLOWED
import com.isel.sensiflow.Constants.Problem.Title.REQUIRED_PARAMETER_MISSING
import com.isel.sensiflow.Constants.Problem.Title.VALIDATION_ERROR
import com.isel.sensiflow.Constants.Problem.URI.INVALID_JSON_BODY
import com.isel.sensiflow.Constants.Problem.URI.URI_HANDLER_NOT_FOUND
import com.isel.sensiflow.Constants.Problem.URI.URI_METHOD_NOT_ALLOWED
import com.isel.sensiflow.Constants.Problem.URI.URI_REQUIRED_PATH_PARAMETER_MISSING
import com.isel.sensiflow.Constants.Problem.URI.URI_VALIDATION_ERROR
import com.isel.sensiflow.services.ServiceException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.TypeMismatchException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.net.URI

// @RestControllerAdvice because all handlers expect a response body
@RestControllerAdvice
class ErrorHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(ServiceException::class)
    fun handleServiceException(ex: ServiceException, request: HttpServletRequest): ProblemDetail {
        val problemDetail = ProblemDetail.forStatus(ex.httpCode).apply {
            type = ex.errorURI
            title = ex.title
            detail = ex.message
            instance = URI(request.requestURI)
        }
        logger.error(problemDetail.toString())
        return problemDetail
    }

    /**
     * Handles the case when a parameter verified by @Valid has a validation error.
     */
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val errors = ex.bindingResult.fieldErrors.map { it.field + " " + it.defaultMessage }.toList()
        val problemDetail = ProblemDetail.forStatus(status).apply {
            type = URI(URI_VALIDATION_ERROR)
            title = VALIDATION_ERROR
            detail = errors[0]
            instance = URI(request.contextPath)
        }
        logger.error(problemDetail.toString())
        return problemDetail.toResponseEntity()
    }

    /**
     * Handles the case when a parameter has the wrong type.
     */
    override fun handleTypeMismatch(
        ex: TypeMismatchException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val problemDetail = ProblemDetail.forStatus(status).apply {
            type = URI(URI_VALIDATION_ERROR)
            title = VALIDATION_ERROR
            detail = ex.message
            instance = URI(request.contextPath)
        }
        logger.error(problemDetail.toString())
        return problemDetail.toResponseEntity()
    }

    /**
     * Handles the case when the received json is not valid.
     */
    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val problemDetail = ProblemDetail.forStatus(status).apply {
            type = URI(INVALID_JSON_BODY)
            title = Constants.Problem.Title.INVALID_JSON_BODY
            detail = ex.message
            instance = URI(request.contextPath)
        }
        logger.error(problemDetail.toString())
        return problemDetail.toResponseEntity()
    }

    /**
     * Handles the case when there are missing parameters.
     */
    override fun handleMissingServletRequestParameter(
        ex: MissingServletRequestParameterException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val problemDetail = ProblemDetail.forStatus(status).apply {
            type = URI(URI_REQUIRED_PATH_PARAMETER_MISSING)
            title = REQUIRED_PARAMETER_MISSING
            detail = ex.message
            instance = URI(request.contextPath)
        }
        logger.error(problemDetail.toString())
        return problemDetail.toResponseEntity()
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception, request: HttpServletRequest): ProblemDetail {
        val problemDetail = ProblemDetail.forStatus(HttpStatusCode.valueOf(500)).apply {
            type = URI(Constants.Problem.URI.SERVICE_INTERNAL)
            title = Constants.Problem.Title.INTERNAL_ERROR
            detail = ex.message
            instance = URI(request.requestURI)
        }
        logger.error(problemDetail.toString())
        return problemDetail
    }

    /**
     * Handle the exception thrown when a handler is not found for a specific request.
     */
    override fun handleNoHandlerFoundException(
        ex: NoHandlerFoundException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val problemDetail = ProblemDetail.forStatus(status).apply {
            type = URI(URI_HANDLER_NOT_FOUND)
            title = HANDLER_NOT_FOUND
            detail = ex.message
            instance = URI(request.contextPath)
        }
        logger.error(problemDetail.toString())
        return problemDetail.toResponseEntity()
    }

    /**
     * Handle the exception thrown when a request method is not supported by the endpoint.
     */
    override fun handleHttpRequestMethodNotSupported(
        ex: HttpRequestMethodNotSupportedException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val problemDetail = ProblemDetail.forStatus(status).apply {
            type = URI(URI_METHOD_NOT_ALLOWED)
            title = METHOD_NOT_ALLOWED
            detail = ex.message
            instance = URI(request.contextPath)
        }
        logger.error(problemDetail.toString())
        return problemDetail.toResponseEntity()
    }
}

private fun ProblemDetail.toResponseEntity(): ResponseEntity<Any> {
    return ResponseEntity
        .status(this.status)
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(this)
}
