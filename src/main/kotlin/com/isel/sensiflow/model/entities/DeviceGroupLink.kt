package com.isel.sensiflow.model.entities

import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.Table

@Entity
@Table(name = "devicegrouplink")
class DeviceGroupLink(
    @EmbeddedId
    val id: DeviceGroupLinkID,

    @MapsId("deviceID")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deviceID", nullable = false)
    val deviceID: Device,

    @MapsId("groupID")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "groupID", nullable = false)
    val groupID: DeviceGroup
)
