package com.github.burkov.dimonserver.model

import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity(name = "Jobs")
data class Job(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = -1,
        val worker_id: String = "",
        val batch_id: Long? = null,
        val params: String = "",
        val due_to: LocalDateTime = LocalDateTime.MIN,
        val retry_count: Int = -1,
        val context: String = "",
        val jobHash: String = ""
)