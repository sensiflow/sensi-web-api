package com.isel.sensiflow.model.repository

import com.isel.sensiflow.model.dao.Metric
import com.isel.sensiflow.model.dao.MetricId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MetricRepository : JpaRepository<Metric, MetricId>
