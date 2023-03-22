package com.isel.sensiflow.model.dao

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import org.hibernate.Hibernate
import java.io.Serializable
import java.sql.Timestamp
import java.util.Objects

@Embeddable
class MetricId(
    @Column(name = "deviceid", nullable = false)
    val deviceID: Int,

    @Column(name = "start_time", nullable = false)
    val startTime: Timestamp
) : Serializable {
    override fun hashCode(): Int = Objects.hash(deviceID, startTime)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

        other as MetricId

        return deviceID == other.deviceID &&
            startTime == other.startTime
    }
}
