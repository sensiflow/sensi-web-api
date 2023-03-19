package com.isel.sensiflow.data.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

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
