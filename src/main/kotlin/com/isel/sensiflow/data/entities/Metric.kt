package com.isel.sensiflow.data.entities

import java.time.Instant
import jakarta.persistence.*

@Entity
@Table(name = "metric")
class Metric {
    @EmbeddedId
    var id: MetricId? = null

    @MapsId("deviceid")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deviceid", nullable = false)
    var deviceid: Device? = null

    @Column(name = "end_time", nullable = false)
    var endTime: Instant? = null

    @Column(name = "peoplecount", nullable = false)
    var peoplecount: Int? = null
}