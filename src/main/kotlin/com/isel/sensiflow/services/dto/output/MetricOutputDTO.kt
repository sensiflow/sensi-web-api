package com.isel.sensiflow.services.dto.output

import com.isel.sensiflow.model.dao.Metric
import java.sql.Timestamp

interface MetricDTO {
    val deviceId: Int
    val startTime: Timestamp
    val endTime: Timestamp
    val peopleCount: Int
}

data class MetricOutputDTO(
    override val deviceId: Int,
    override val startTime: Timestamp,
    override val endTime: Timestamp,
    override val peopleCount: Int
) : MetricDTO

fun Metric.toDTO(): MetricOutputDTO {
    return MetricOutputDTO(
        deviceId = this.id.deviceID,
        startTime = this.id.startTime,
        endTime = this.endTime,
        peopleCount = this.peopleCount
    )
}
