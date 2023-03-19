package com.isel.sensiflow.data.repository;

import com.isel.sensiflow.data.entities.DeviceGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DeviceGroupRepository : JpaRepository<DeviceGroup, Int> {
}