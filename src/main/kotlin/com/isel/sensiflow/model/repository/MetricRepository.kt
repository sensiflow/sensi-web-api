package com.isel.sensiflow.model.repository

import com.isel.sensiflow.model.entities.Device
import com.isel.sensiflow.model.entities.Metric
import com.isel.sensiflow.model.entities.MetricID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.util.Optional

@Repository
interface MetricRepository : JpaRepository<Metric, MetricID> {

    @Query(
        "SELECT m FROM Metric m " +
            "WHERE m.device.id = :deviceID " +
            "ORDER BY m.id.startTime ASC"
    )
    fun findAllByDeviceId(deviceID: Int, pageable: Pageable): Page<Metric>

    /**
     * Finds all metrics associated with the given device and with a start time between the given timestamps.
     */
    @Query(
        "SELECT m FROM Metric m " +
            "WHERE m.device.id = :deviceID " +
            "AND m.id.startTime >= :startTime " +
            "AND m.id.startTime <= :endTime " +
            "ORDER BY m.id.startTime ASC"
    )
    fun findAllBetween(startTime: Timestamp, endTime: Timestamp, deviceID: Int, pageable: Pageable): Page<Metric>

    /**
     * Finds all metrics associated with the given device and with a start time after the given timestamp.
     */
    @Query(
        "SELECT m FROM Metric m " +
            "WHERE m.device.id = :deviceID " +
            "AND m.id.startTime >= :startTime " +
            "ORDER BY m.id.startTime ASC"
    )
    fun findAllAfter(startTime: Timestamp, deviceID: Int, pageable: Pageable): Page<Metric>

    /**
     * Finds all metrics associated with the given device and with a start time before the given timestamp.
     */
    @Query(
        "SELECT m FROM Metric m " +
            "WHERE m.device.id = :deviceID " +
            "AND m.id.startTime <= :endTime " +
            "ORDER BY m.id.startTime ASC"
    )
    fun findAllBefore(endTime: Timestamp, deviceID: Int, pageable: Pageable): Page<Metric>

    /**
     * Deletes all metrics associated with the given device.
     */
    fun deleteAllByDevice(device: Device)

    /**
     * Finds the metric with the maximum start time associated with the given device.
     */
    @Query(
        "SELECT m FROM Metric m " +
            "WHERE m.device.id = :deviceID " +
            "AND m.id.startTime = (SELECT MAX(m.id.startTime) FROM Metric m)"
    )
    fun findByMaxStartTime(deviceID: Int): Optional<Metric>
}
