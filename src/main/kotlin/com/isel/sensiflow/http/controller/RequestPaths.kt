package com.isel.sensiflow.http.controller

object RequestPaths {

    object Device {
        const val DEVICE = "/devices"
        const val DEVICE_ID = "/{id}"
        const val PROCESSING_STATE = "$DEVICE_ID/processing-state"
    }
}
