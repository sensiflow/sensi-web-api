package com.isel.sensiflow.model.repository

import com.isel.sensiflow.model.entities.Device
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DeviceRepository : JpaRepository<Device, Int>
