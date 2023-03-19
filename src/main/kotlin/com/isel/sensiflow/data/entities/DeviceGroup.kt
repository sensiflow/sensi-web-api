package com.isel.sensiflow.data.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "devicegroup")
class DeviceGroup {
    @Id
    @Column(name = "id", nullable = false)
    var id: Int? = null

    @Column(name = "name", nullable = false, length = 30)
    var name: String? = null

    @Column(name = "description", nullable = false)
    var description: String? = null
}
