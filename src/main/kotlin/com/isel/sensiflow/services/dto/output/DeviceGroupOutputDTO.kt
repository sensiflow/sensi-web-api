package com.isel.sensiflow.services.dto.output

import com.isel.sensiflow.model.dao.DeviceGroup
import com.isel.sensiflow.services.ID

interface DeviceGroupOutputDTO {
    val id: ID
    val name: String
    val description: String?
}

/**
 * Simple DeviceGroup data transfer object only with ids.
 * @param id The deviceGroup id
 * @param name The deviceGroup name
 * @param description The deviceGroup description
 * @param devices The deviceGroup devices
 */
data class DeviceGroupSimpleOutputDTO(
    override val id: ID,
    override val name: String,
    override val description: String?,
    val devices: List<ID>,
) : DeviceGroupOutputDTO

/**
 * Expanded DeviceGroup data transfer object with all the information.
 * @param id The deviceGroup id
 * @param name The deviceGroup name
 * @param description The deviceGroup description
 * @param devices The deviceGroup devices
 */
data class DeviceGroupExpandedOutputDTO(
    override val id: ID,
    override val name: String,
    override val description: String?,
    val devices: List<DeviceOutputDTO>,
) : DeviceGroupOutputDTO

fun DeviceGroup.toDTO(expanded: Boolean): DeviceGroupOutputDTO {
    return if (expanded) {
        DeviceGroupExpandedOutputDTO(
            id = this.id,
            name = this.name,
            description = this.description,
            devices = this.devices.map { it.toDTO(expanded = false) }
        )
    } else {
        DeviceGroupSimpleOutputDTO(
            id = this.id,
            name = this.name,
            description = this.description,
            devices = this.devices.map { it.id }
        )
    }
}
