package com.isel.sensiflow.model.repository

import com.isel.sensiflow.model.dao.ProcessedStream
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProcessedStreamRepository : JpaRepository<ProcessedStream, Int>
