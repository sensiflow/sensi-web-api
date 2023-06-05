package com.isel.sensiflow.model.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

@Entity
@Table(name = "devicegroup")
class DeviceGroup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Int = -1,

    @Column(name = "name", nullable = false, length = 30)
    val name: String,

    @Column(name = "description", nullable = true)
    val description: String?
) {
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "devicegrouplink",
        joinColumns = [JoinColumn(name = "groupid")],
        inverseJoinColumns = [JoinColumn(name = "deviceid")]
    )
    val devices: MutableSet<Device> = mutableSetOf()
}
