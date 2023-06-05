package com.isel.sensiflow.model.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.sql.Timestamp

@Entity
@Table(name = "sessiontoken")
class SessionToken(
    @Id
    @Column(name = "token", nullable = false)
    val token: String,

    @Column(name = "expiration", nullable = false)
    val expiration: Timestamp,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid")
    val user: User
)

/**
 * Checks if the session token has expired.
 */
fun SessionToken.hasExpired(): Boolean {
    return expiration.before(Timestamp(System.currentTimeMillis()))
}
