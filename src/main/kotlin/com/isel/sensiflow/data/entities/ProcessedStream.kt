package com.isel.sensiflow.data.entities

import jakarta.persistence.*

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