package com.isel.sensiflow.model.dao

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "processedstream")
class ProcessedStream(
    @Id
    @Column(name = "deviceid", nullable = false)
    val id: Int = -1,

    @Column(name = "streamurl", nullable = false, length = 200)
    val processedStreamURL: String,

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = [jakarta.persistence.CascadeType.ALL])
    @JoinColumn(name = "deviceid", nullable = false)
    val device: Device
)
