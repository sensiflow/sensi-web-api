package com.isel.sensiflow.services.dto

import com.isel.sensiflow.model.entities.Device

interface DeviceOutputDTO {
    val id: Int
    val name: String
    val description: String?
    val streamUrl: String
}

/**
 * Simple Device data transfer object only with ids.
 * @param id The device id
 * @param name The device name
 * @param description The device description
 * @param streamUrl The device stream url
 */
data class DeviceSimpleOutputDTO(
    override val id: Int,
    override val name: String,
    override val description: String?,
    override val streamUrl: String,
    // TODO: Add UserId
    // TODO: Add DeviceGroupId
) : DeviceOutputDTO

/**
 *
 */
data class DeviceExpandedOutputDTO(
    override val id: Int,
    override val name: String,
    override val description: String?,
    override val streamUrl: String,
    // TODO : val user: UserOutputDTO Adiciona aqui o teu,
    // TODO e: val deviceGroup: DeviceGroupOutputDTO Adiciona aqui o teu,
) : DeviceOutputDTO

fun Device.toDTO(expanded: Boolean): DeviceOutputDTO {
    return if (expanded) {
        DeviceExpandedOutputDTO(
            id = this.id,
            name = this.name,
            description = this.description,
            streamUrl = this.streamurl,
            // TODO : user = this.user.toDTO(expanded = false)
            // TODO : deviceGroup = this.deviceGroup.toDTO(expanded = false)
        )
    } else {
        DeviceSimpleOutputDTO(
            id = this.id,
            name = this.name,
            description = this.description,
            streamUrl = this.streamurl,
            // TODO : userId = this.user.id,
            // TODO : deviceGroupId = this.deviceGroup.id,
        )
    }
}
