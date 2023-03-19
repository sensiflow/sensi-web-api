package com.isel.sensiflow.entities

import javax.persistence.*

@Entity(name = "User")
@Table(name = "user")
class User(
    @Id
    @Column(name = "id", nullable = false)
    var id: Long,

    @Column(name = "first_name", nullable=false, unique=true)
    var firstName: String,

    @Column(name = "last_name", nullable=false)
    var lastName: String,

    @Column(name = "password_hash", nullable=false)
    var passwordHash: String,

    @Column(name = "password_salt", nullable=false)
    var passwordSalt: String,

    @Column(name = "email", nullable=false, unique=true)
    var email: String,

    @Column(name = "name", nullable=false)
    var name: String,
)