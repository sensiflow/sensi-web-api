package com.isel.sensiflow.services.dto.output

import com.isel.sensiflow.model.dao.DeviceGroup
import com.isel.sensiflow.services.ID

data class DeviceGroupOutputDTO(
    val id: ID,
    val name: String,
    val description: String?
)

fun DeviceGroup.toDeviceGroupOutputDTO(): DeviceGroupOutputDTO {
    return DeviceGroupOutputDTO(
        id = this.id,
        name = this.name,
        description = this.description
    )
}
