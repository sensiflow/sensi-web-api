package com.isel.sensiflow.model.dao

import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.Table
import java.sql.Timestamp

@Entity
@Table(name = "metric")
class Metric(
    @EmbeddedId
    val id: MetricID,

    @Column(name = "end_time", nullable = false)
    val endTime: Timestamp,

    @Column(name = "peoplecount", nullable = false)
    val peopleCount: Int,

    @MapsId("deviceID")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deviceID", nullable = false)
    val device: Device
)
