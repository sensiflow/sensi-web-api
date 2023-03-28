package com.isel.sensiflow.services.dto.output

import com.isel.sensiflow.http.entities.output.UserOutput
import com.isel.sensiflow.http.entities.output.toDTO
import com.isel.sensiflow.model.dao.Device
import com.isel.sensiflow.services.ID
import com.isel.sensiflow.services.UserID

interface DeviceOutputDTO {
    val id: ID
    val name: String
    val description: String?
    val streamURL: String
    val processingState: String
}

/**
 * Simple Device data transfer object only with ids.
 * @param id The device id
 * @param name The device name
 * @param description The device description
 * @param streamURL The device stream url
 * @param userID The device user id
 */
data class DeviceSimpleOutputDTO(
    override val id: ID,
    override val name: String,
    override val description: String?,
    override val streamURL: String,
    override val processingState: String,
    val userID: UserID,
    // TODO: Add DeviceGroupId
) : DeviceOutputDTO

/**
 * Expanded Device data transfer object with all the information.
 * @param id The device id
 * @param name The device name
 * @param description The device description
 * @param streamURL The device stream url
 * @param user The device user
 */
data class DeviceExpandedOutputDTO(
    override val id: ID,
    override val name: String,
    override val description: String?,
    override val streamURL: String,
    override val processingState: String,
    val user: UserOutput,
    // TODO e: val deviceGroup: DeviceGroupOutputDTO Adiciona aqui o teu,
) : DeviceOutputDTO

/**
 * Factory method to create a DeviceOutputDTO from a Device
 * @param expanded If true, the DeviceOutputDTO foreign key fields will be expanded to the full entity
 */
fun Device.toDTO(expanded: Boolean): DeviceOutputDTO {

    val processingStateString = this.processingState.toString()

    return if (expanded) {
        DeviceExpandedOutputDTO(
            id = this.id,
            name = this.name,
            description = this.description,
            streamURL = this.streamURL,
            processingState = processingStateString,
            user = this.user.toDTO(),
            // TODO : deviceGroup = this.deviceGroup.toDTO(expanded = false)
        )
    } else {
        DeviceSimpleOutputDTO(
            id = this.id,
            name = this.name,
            description = this.description,
            streamURL = this.streamURL,
            processingState = processingStateString,
            userID = this.user.id,
            // TODO : deviceGroupId = this.deviceGroup.id,
        )
    }
}