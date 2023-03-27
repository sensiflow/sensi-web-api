package com.isel.sensiflow.http.controller

object RequestPaths {

    object Device {
        const val DEVICE = "/devices"
        const val DEVICE_ID = "/{id}"
    }

    object Users {
        const val LOGIN = "/users/login"
        const val REGISTER = "/users"
        const val LOGOUT = "/users/logout"
        const val GET_USER = "/users/{userID}"
    }
}
