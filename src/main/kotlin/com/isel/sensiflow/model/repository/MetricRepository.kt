package com.isel.sensiflow.model.repository

import com.isel.sensiflow.model.entities.Device
import com.isel.sensiflow.model.entities.Metric
import com.isel.sensiflow.model.entities.MetricID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface MetricRepository : JpaRepository<Metric, MetricID> {

    @Query("SELECT m FROM Metric m " +
            "WHERE m.device.id = :deviceID " +
            "ORDER BY m.id.startTime ASC")
    fun findAllByDeviceId(deviceID: Int, pageable: Pageable): Page<Metric>

    fun deleteAllByDevice(device: Device)
    fun deleteAllByDeviceIn(devices: List<Device>)

    @Query(
        "SELECT m FROM Metric m " +
            "WHERE m.device.id = :deviceID " +
            "AND m.id.startTime = (SELECT MAX(m.id.startTime) FROM Metric m)"
    )
    fun findByMaxStartTime(deviceID: Int): Optional<Metric>
}
