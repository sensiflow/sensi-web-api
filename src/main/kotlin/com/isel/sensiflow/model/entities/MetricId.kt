package com.isel.sensiflow.model.entities

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import org.hibernate.Hibernate
import java.io.Serializable
import java.time.Instant
import java.util.Objects

@Embeddable
class MetricId : Serializable {
    @Column(name = "deviceid", nullable = false)
    var deviceid: Int? = null

    @Column(name = "start_time", nullable = false)
    var startTime: Instant? = null

    override fun hashCode(): Int = Objects.hash(deviceid, startTime)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

        other as MetricId

        return deviceid == other.deviceid &&
            startTime == other.startTime
    }
}
