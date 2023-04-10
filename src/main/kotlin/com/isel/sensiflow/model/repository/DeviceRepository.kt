package com.isel.sensiflow.model.repository

import com.isel.sensiflow.model.dao.Device
import com.isel.sensiflow.services.DeviceNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DeviceRepository : JpaRepository<Device, Int>

/**
 * Wrapper for [DeviceRepository.findAllById] that throws a [DeviceNotFoundException] when a device is not found
 */
fun DeviceRepository.requireFindAllById(ids: List<Int>): List<Device> {
    try {
        val foundDevices = this.findAllById(ids)
        require(foundDevices.size == ids.size)
        return foundDevices
    } catch (e: IllegalArgumentException) {
        throw DeviceNotFoundException(2)
    }
}
