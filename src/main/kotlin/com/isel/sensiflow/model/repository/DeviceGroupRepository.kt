package com.isel.sensiflow.model.repository

import com.isel.sensiflow.model.entities.Device
import com.isel.sensiflow.model.entities.DeviceGroup
import com.isel.sensiflow.services.ID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface DeviceGroupRepository : JpaRepository<DeviceGroup, Int> {
    @Query("SELECT d FROM Device d JOIN d.deviceGroups dg WHERE dg.id = :groupID")
    fun findPaginatedByEntityDeviceId(@Param("groupID") groupID: ID, pageable: Pageable): Page<Device>
}
