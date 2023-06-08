package com.isel.sensiflow.http.controller

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping()
class RedirectController : ErrorController {

    @GetMapping("/error")
    fun redirectToIndex(): String {
        return "forward:/index.html"
    }
}
