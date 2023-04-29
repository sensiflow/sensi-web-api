package com.isel.sensiflow.model.dao

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Type

@Entity
@Table(name = "device")
class Device(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Int = -1,

    @Column(name = "name", nullable = false, length = 20)
    val name: String,
    @Column(name = "streamurl", nullable = false, length = 200)
    val streamURL: String,

    @Column(name = "description", nullable = true)
    val description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "processingstate", nullable = false)
    @Type(PostgreSQLEnumType::class)
    val processingState: DeviceProcessingState = DeviceProcessingState.INACTIVE,

    @Column(name = "pending_update", nullable = false)
    val pendingUpdate: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid")
    val user: User
) {

    @OneToOne(mappedBy = "device")
    var processedStream: ProcessedStream? = null

    @OneToMany(mappedBy = "device")
    val metrics: MutableSet<Metric> = mutableSetOf()

    @ManyToMany(mappedBy = "devices")
    val deviceGroups: MutableSet<DeviceGroup> = mutableSetOf()

    @Column(name = "scheduled_for_deletion", nullable = false)
    private val scheduledForDeletion: Boolean = false
}
