package com.isel.sensiflow.model.entities

import com.isel.sensiflow.Constants
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "device")
class Device(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int = -1,

    @Column(name = "name", nullable = false, length = Constants.Device.NAME_MAX_LENGTH)
    var name: String,

    @Column(name = "streamurl", nullable = false, length = Constants.Device.STREAM_URL_MAX_LENGTH)
    var streamurl: String,

    @Column(name = "description", nullable = false, length = Constants.Device.DESCRIPTION_MAX_LENGTH)
    var description: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid")
    var user: User
)
