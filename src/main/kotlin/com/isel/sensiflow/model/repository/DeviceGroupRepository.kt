package com.isel.sensiflow.model.repository

import com.isel.sensiflow.model.dao.Device
import com.isel.sensiflow.model.dao.DeviceGroup
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface DeviceGroupRepository : JpaRepository<DeviceGroup, Int> {
    @Query("SELECT d FROM DeviceGroupLink dl JOIN Device d ON dl.deviceID = d.id WHERE dl.groupID = :groupID")
    fun findAllDevicesByGroupId(groupID: Int, pageable: Pageable): Page<Device>
}
