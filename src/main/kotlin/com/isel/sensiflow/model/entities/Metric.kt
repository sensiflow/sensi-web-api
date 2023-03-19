package com.isel.sensiflow.model.entities

import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.Table
import java.time.Instant

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
