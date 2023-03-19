package com.isel.sensiflow.data.repository;

import com.isel.sensiflow.data.entities.Metric
import com.isel.sensiflow.data.entities.MetricId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MetricRepository : JpaRepository<Metric, MetricId> {
}