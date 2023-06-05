package com.isel.sensiflow.model.entities

import com.isel.sensiflow.services.dto.UserDTO
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role", nullable = false)
    val role: Userrole,

    @Column(name = "password_hash", nullable = false, length = 200)
    val passwordHash: String,

    @Column(name = "password_salt", nullable = false, length = 32)
    val passwordSalt: String,
) {

    @OneToOne(mappedBy = "user", cascade = [jakarta.persistence.CascadeType.REMOVE], orphanRemoval = true)
    lateinit var email: Email

    @OneToMany(mappedBy = "user", cascade = [jakarta.persistence.CascadeType.REMOVE], orphanRemoval = true )
    val sessionTokens: MutableSet<SessionToken> = mutableSetOf() //TODO: pensar na tabela dos tokens se é necessario

    fun isEmailInitialized() = this::email.isInitialized
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
    if (!isEmailInitialized())
        throw IllegalStateException("User email not initialized")//TODO: no get de todos os users isto ta a dar erro n sei pq

    return UserDTO(
        id = this.id,
        email = email.email,
        role = this.role.toRole(),
        firstName = this.firstName,
        lastName = this.lastName,
    )
}
