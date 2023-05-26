package com.isel.sensiflow.services.dto.output

import com.isel.sensiflow.model.dao.DeviceGroup
import com.isel.sensiflow.services.ID
import com.isel.sensiflow.services.dto.PageableDTO

interface DeviceGroupOutputDTO {
    val id: ID
    val name: String
    val description: String?
}

data class DeviceGroupSimpleOutputDTO(
    override val id: ID,
    override val name: String,
    override val description: String?
) : DeviceGroupOutputDTO

data class DeviceGroupOutputExpandedDTO(
    override val id: ID,
    override val name: String,
    override val description: String?,
    val devices: PageDTO<DeviceSimpleOutputDTO>
) : DeviceGroupOutputDTO

fun DeviceGroup.toDeviceGroupOutputDTO(expanded: Boolean, devicesPaginationModel: PageableDTO? = null): DeviceGroupOutputDTO {
    return if (!expanded) {
        DeviceGroupSimpleOutputDTO(
            id = this.id,
            name = this.name,
            description = this.description
        )
    } else {
        require(devicesPaginationModel != null) { "devicesPaginationModel must not be null when expanded is true" }
        DeviceGroupOutputExpandedDTO(
            id = this.id,
            name = this.name,
            description = this.description,
            devices = this.devices.toList()
                .map { it.toDeviceOutputDTO(expanded = false) as DeviceSimpleOutputDTO }
                .toPageDTO(devicesPaginationModel)
        )
    }
}
