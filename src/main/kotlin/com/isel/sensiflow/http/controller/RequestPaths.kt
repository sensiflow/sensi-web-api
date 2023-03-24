package com.isel.sensiflow.http.controller

object RequestPaths {

    object Device {
        const val DEVICE = "/devices"
        const val DEVICE_ID = "$/{id}"
        const val DEVICE_STATS = "$DEVICE_ID/stats"
        const val DEVICE_PROCESSED_STREAM = "$DEVICE_ID/processed-stream"
    }

    object DeviceGroups {
        const val GROUP = "/groups"
        const val GROUP_ID = "$/{id}"
        const val GROUPS_DEVICES = "$GROUP_ID/devices"
    }
}
