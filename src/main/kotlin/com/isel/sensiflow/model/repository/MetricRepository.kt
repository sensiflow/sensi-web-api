package com.isel.sensiflow.model.repository

import com.isel.sensiflow.model.dao.Metric
import com.isel.sensiflow.model.dao.MetricID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MetricRepository : JpaRepository<Metric, MetricID> {
    fun findAllByDeviceID(deviceId: Int, pageable: Pageable): Page<Metric>
}
