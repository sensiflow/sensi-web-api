package com.isel.sensiflow.model.repository

import com.isel.sensiflow.model.entities.DeviceGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DeviceGroupRepository : JpaRepository<DeviceGroup, Int>
