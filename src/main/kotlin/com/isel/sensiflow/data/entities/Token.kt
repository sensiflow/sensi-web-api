package com.isel.sensiflow.data.entities

import jakarta.persistence.*

@Entity
@Table(name = "token")
class Token {
    @Id
    @Column(name = "token", nullable = false)
    var id: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid")
    var userid: User? = null
}