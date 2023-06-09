package com.isel.sensiflow.model.repository

import com.isel.sensiflow.model.entities.Device
import com.isel.sensiflow.services.DeviceNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface DeviceRepository : JpaRepository<Device, Int> {

    @Query("SELECT d FROM Device d WHERE d.id = :id AND d.scheduledForDeletion = false")
    override fun findById(id: Int): Optional<Device>

    @Query("SELECT d FROM Device d WHERE d.scheduledForDeletion = false and d.id IN :ids")
    override fun findAllById(ids: Iterable<Int>): List<Device>

    @Query(
        "SELECT * FROM device d WHERE d.scheduled_for_deletion = false" +
            " AND LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%'))",
        nativeQuery = true
    )
    fun findAll(@Param("search") search: String?, pageable: Pageable): Page<Device>

    @Query("SELECT d FROM Device d WHERE d.scheduledForDeletion = false")
    override fun findAll(pageable: Pageable): Page<Device>

    @Modifying
    @Query("UPDATE Device d SET d.scheduledForDeletion = true WHERE d IN :device")
    fun flagForDeletion(device: List<Device>)
}

/**
 * Wrapper for [DeviceRepository.findAllById] that throws a [DeviceNotFoundException] when a device is not found
 */
fun DeviceRepository.requireFindAllById(ids: List<Int>): List<Device> {
    try {
        val foundDevices = this.findAllById(ids)
        val notFoundDevice = ids.firstOrNull { id -> foundDevices.none { it.id == id } }
        require(foundDevices.size == ids.size) { "$notFoundDevice" }
        return foundDevices
    } catch (e: IllegalArgumentException) {
        val deviceID = e.message?.toInt()
        require(deviceID != null)
        throw DeviceNotFoundException(deviceID)
    }
}
