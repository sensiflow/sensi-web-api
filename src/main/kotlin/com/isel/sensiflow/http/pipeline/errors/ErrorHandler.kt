package com.isel.sensiflow.http.pipeline.errors

import com.isel.sensiflow.Constants.Problem.Title.HANDLER_NOT_FOUND
import com.isel.sensiflow.Constants.Problem.Title.METHOD_NOT_ALLOWED
import com.isel.sensiflow.Constants.Problem.Title.VALIDATION_ERROR
import com.isel.sensiflow.Constants.Problem.URI.URI_HANDLER_NOT_FOUND
import com.isel.sensiflow.Constants.Problem.URI.URI_METHOD_NOT_ALLOWED
import com.isel.sensiflow.Constants.Problem.URI.URI_VALIDATION_ERROR
import com.isel.sensiflow.services.ServiceException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.net.URI

// @RestControllerAdvice because all handlers expect a response body
@RestControllerAdvice
class ErrorHandler : ResponseEntityExceptionHandler() {

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
        return problemDetail.toResponseEntity()
    }

    @ExceptionHandler(ServiceException::class)
    fun handleServiceException(ex: ServiceException, request: HttpServletRequest): ProblemDetail {
        val problemDetail = ProblemDetail.forStatus(ex.httpCode).apply {
            type = ex.errorURI
            title = ex.title
            detail = ex.message
            instance = URI(request.requestURI)
        }
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
        return problemDetail.toResponseEntity()
    }

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
        return problemDetail.toResponseEntity()
    }
}

private fun ProblemDetail.toResponseEntity(): ResponseEntity<Any>? {
    return ResponseEntity
        .status(this.status)
        .contentType(MediaType.parseMediaType("application/problem+json"))
        .body(this)
}
