package com.isel.sensiflow.data.repository

import com.isel.sensiflow.data.entities.ProcessedStream
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProcessedStreamRepository : JpaRepository<ProcessedStream, Int>
