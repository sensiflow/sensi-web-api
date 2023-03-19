package com.isel.sensiflow.data.entities

import org.hibernate.Hibernate
import java.io.Serializable
import java.util.*
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class DeviceGroupLinkId : Serializable {
    @Column(name = "deviceid", nullable = false)
    var deviceid: Int? = null

    @Column(name = "groupid", nullable = false)
    var groupid: Int? = null

    override fun hashCode(): Int = Objects.hash(deviceid, groupid)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

        other as DeviceGroupLinkId

        return deviceid == other.deviceid &&
                groupid == other.groupid
    }

}