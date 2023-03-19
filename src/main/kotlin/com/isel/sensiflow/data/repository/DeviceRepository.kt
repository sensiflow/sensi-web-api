package com.isel.sensiflow.data.repository

import com.isel.sensiflow.data.entities.Device
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DeviceRepository : JpaRepository<Device, Int>
