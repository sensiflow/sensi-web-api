package com.isel.sensiflow.data.entities

import jakarta.persistence.*

@Entity
@Table(name = "email")
class Email {
    @Id
    @Column(name = "email", nullable = false, length = 100)
    var id: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid")
    var userid: User? = null
}