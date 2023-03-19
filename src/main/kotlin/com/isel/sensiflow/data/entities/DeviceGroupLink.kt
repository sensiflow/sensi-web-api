package com.isel.sensiflow.data.entities

import jakarta.persistence.*

@Entity
@Table(name = "devicegrouplink")
class DeviceGroupLink {
    @EmbeddedId
    var id: DeviceGroupLinkId? = null

    @MapsId("deviceid")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deviceid", nullable = false)
    var deviceid: Device? = null

    @MapsId("groupid")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "groupid", nullable = false)
    var groupid: DeviceGroup? = null
}