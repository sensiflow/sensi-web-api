package com.isel.sensiflow.http.controller

object RequestPaths {
    object Device {
        const val DEVICE = "/devices"
        const val DEVICE_ID = "/{id}"
        const val PROCESSING_STATE = "$DEVICE_ID/processing-state"
        const val DEVICE_STATS = "$DEVICE_ID/stats"
        const val PEOPLE_COUNT_STREAM = "$DEVICE_ID/server-events/people-count"
    }

    object DeviceGroups {
        const val GROUP = "/groups"
        const val GROUP_ID = "/{id}"
        const val GROUPS_DEVICES = "$GROUP_ID/devices"
    }

    object Users {
        const val USERS = "/users"
        const val LOGIN = "/login"
        const val LOGOUT = "/logout"
        const val GET_USER = "/{id}"
        const val ROLE = "{id}/role"
        const val MYINFO = "/myinfo"
    }

    object SSE {
        const val SSE = "/server-events"
        const val SSE_DEVICE_STATE = "$SSE/processing-state"
    }
}
