package com.isel.sensiflow.controller.controllers

import com.isel.sensiflow.services.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(private val userService: UserService) {

    @GetMapping("/example")
    fun example1(): Long {
        return userService.example()
    }

}