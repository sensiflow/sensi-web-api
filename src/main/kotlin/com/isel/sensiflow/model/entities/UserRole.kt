package com.isel.sensiflow.model.entities

import com.isel.sensiflow.services.Role
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.Size

@Entity
@Table(name = "userrole")
class UserRole(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Int,

    @Size(max = 30)
    @Column(name = "role", nullable = false, length = 30)
    val role: String

) {
    @OneToMany(mappedBy = "role")
    val users: MutableSet<User> = mutableSetOf()
}

fun UserRole.toRole() = Role.valueOf(this.role)
