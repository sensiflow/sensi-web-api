package com.isel.sensiflow.model.repository

import com.isel.sensiflow.model.dao.Device
import com.isel.sensiflow.model.dao.Metric
import com.isel.sensiflow.model.dao.MetricID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface MetricRepository : JpaRepository<Metric, MetricID> {
    fun findAllByDeviceId(deviceID: Int, pageable: Pageable): Page<Metric>

    fun deleteAllByDevice(device: Device)

    @Query(
        "SELECT m FROM Metric m " +
            "WHERE m.device.id = :deviceID " +
            "AND m.id.startTime = (SELECT MAX(m.id.startTime) FROM Metric m)"
    )
    fun findByMaxStartTime(deviceID: Int): Optional<Metric>
}
