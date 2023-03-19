package com.isel.sensiflow.model.entities

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
class ProcessedStream {
    @Id
    @Column(name = "deviceid", nullable = false)
    var id: Int? = null

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deviceid", nullable = false)
    var device: Device? = null

    @Column(name = "streamurl", nullable = false, length = 200)
    var streamurl: String? = null
}
