package com.isel.sensiflow.model.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "\"User\"")
class User {
    @Id
    @Column(name = "id", nullable = false)
    var id: Int? = null

    @Column(name = "first_name", nullable = false, length = 20)
    var firstName: String? = null

    @Column(name = "last_name", nullable = false, length = 20)
    var lastName: String? = null

    @Column(name = "password_hash", nullable = false, length = 200)
    var passwordHash: String? = null

    @Column(name = "password_salt", nullable = false, length = 32)
    var passwordSalt: String? = null
}
