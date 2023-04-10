package com.isel.sensiflow.model.dao

import com.isel.sensiflow.services.Role
import com.isel.sensiflow.services.dto.UserDTO
import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Type

@Entity
@Table(name = "\"user\"")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Int = -1,

    @Column(name = "first_name", nullable = false, length = 20)
    val firstName: String,

    @Column(name = "last_name", nullable = false, length = 20)
    val lastName: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Type(PostgreSQLEnumType::class)
    val role: Role = Role.USER,

    @Column(name = "password_hash", nullable = false, length = 200)
    val passwordHash: String,

    @Column(name = "password_salt", nullable = false, length = 32)
    val passwordSalt: String,
) {
    @OneToMany(mappedBy = "user")
    val devices: MutableSet<Device> = mutableSetOf()

    @OneToOne(mappedBy = "user")
    var email: Email? = null

    @OneToMany(mappedBy = "user")
    val sessionTokens: MutableSet<SessionToken> = mutableSetOf()
}

/**
 * Adds an email to a user
 * @param email the email to add
 * @return the user
 */
fun User.addEmail(email: Email): User {
    this.email = email
    return this
}

/**
 * Converts a [User] to a [UserDTO]
 */
fun User.toDTO(): UserDTO {
    val email = this.email
    require(email != null) { "Invalid user creation" }
    return UserDTO(
        email = email.email,
        role = this.role,
        firstName = this.firstName,
        lastName = this.lastName,
    )
}
