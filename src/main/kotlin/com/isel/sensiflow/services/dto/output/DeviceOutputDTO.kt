package com.isel.sensiflow.services.dto.output

import com.isel.sensiflow.model.entities.Device
import com.isel.sensiflow.services.ID

interface DeviceOutputDTO {
    val id: ID
    val name: String
    val description: String?
    val streamURL: String
    val processingState: DeviceProcessingStateOutput
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
    override val processingState: DeviceProcessingStateOutput,
    val deviceGroupsID: List<Int>
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
    override val processingState: DeviceProcessingStateOutput,
    val deviceGroups: List<DeviceGroupOutputDTO>
) : DeviceOutputDTO

/**
 * Factory method to create a DeviceOutputDTO from a Device
 * @param expanded If true, the DeviceOutputDTO foreign key fields will be expanded to the full entity
 */
fun Device.toDeviceOutputDTO(expanded: Boolean): DeviceOutputDTO {
    val processingStateOutput = this.processingStateOutput

    return if (expanded) {
        DeviceExpandedOutputDTO(
            id = this.id,
            name = this.name,
            description = this.description,
            streamURL = this.streamURL,
            processingState = processingStateOutput,
            deviceGroups = this.deviceGroups
                .map { it.toDeviceGroupOutputDTO(expanded = false) }

        )
    } else {
        DeviceSimpleOutputDTO(
            id = this.id,
            name = this.name,
            description = this.description,
            streamURL = this.streamURL,
            processingState = processingStateOutput,
            deviceGroupsID = this.deviceGroups.map { it.id }
        )
    }
}

val Device.processingStateOutput: DeviceProcessingStateOutput
    get() = if (this.pendingUpdate)
        DeviceProcessingStateOutput.PENDING
    else
        this.processingState.toDeviceProcessingStateOutput()
