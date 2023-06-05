package com.isel.sensiflow.model.repository

import com.isel.sensiflow.model.entities.Device
import com.isel.sensiflow.model.entities.ProcessedStream
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProcessedStreamRepository : JpaRepository<ProcessedStream, Int> {

    fun deleteAllByDevice(device: Device)
    fun deleteAllByDeviceIn(devices: List<Device>)
}
