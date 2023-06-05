package com.isel.sensiflow.model.entities

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import org.hibernate.Hibernate
import java.io.Serializable
import java.util.Objects

@Embeddable
class DeviceGroupLinkID(
    @Column(name = "deviceID", nullable = false)
    val deviceID: Int = -1,

    @Column(name = "groupID", nullable = false)
    val groupID: Int = -1
) : Serializable {
    override fun hashCode(): Int = Objects.hash(deviceID, groupID)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

        other as DeviceGroupLinkID

        return deviceID == other.deviceID &&
            groupID == other.groupID
    }
}
