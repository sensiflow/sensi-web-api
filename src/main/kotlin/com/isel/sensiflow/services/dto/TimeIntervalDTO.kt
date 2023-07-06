package com.isel.sensiflow.services.dto

import java.sql.Timestamp

data class TimeIntervalDTO(
    val startTime: Timestamp? = null,
    val endTime: Timestamp? = null
)
