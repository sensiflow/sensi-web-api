package com.isel.sensiflow.services.dto.output

import com.isel.sensiflow.model.dao.Metric
import com.isel.sensiflow.services.ID
import java.sql.Timestamp

interface MetricDTO {
    val deviceID: ID
    val startTime: Timestamp
    val endTime: Timestamp
    val peopleCount: Int
}

data class MetricOutputDTO(
    override val deviceID: ID,
    override val startTime: Timestamp,
    override val endTime: Timestamp,
    override val peopleCount: Int
) : MetricDTO

fun Metric.toMetricOutputDTO(): MetricOutputDTO {
    return MetricOutputDTO(
        deviceID = this.id.deviceID,
        startTime = this.id.startTime,
        endTime = this.endTime,
        peopleCount = this.peopleCount
    )
}
