package com.github.burkov.dpmonserver.models

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity(name = "Jobs")
data class Job(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Int = 0,
        val params: String = ""
)