package com.isel.sensiflow.http.controller

import com.isel.sensiflow.Constants
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.net.URI

@Controller
@RequestMapping()
class RedirectController : ErrorController {

    // Shows an error to every request beginning with the api root that does not belong to the api
    @RequestMapping(RequestPaths.Root.ROOT + "/*")
    @ResponseBody
    fun fallbackNotFound(request: HttpServletRequest): ResponseEntity<ProblemDetail> {
        val problemDetail = ProblemDetail.forStatus(404).apply {
            type = URI(Constants.Problem.URI.URI_HANDLER_NOT_FOUND)
            title = Constants.Problem.Title.HANDLER_NOT_FOUND
            instance = URI(request.contextPath)
        }
        return ResponseEntity
            .status(problemDetail.status)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problemDetail)
    }

    // From every request with an uri that does not belong to the api redirects to the index.html
    @GetMapping("/error")
    fun redirectToIndex(): String {
        return "forward:/index.html"
    }
}
