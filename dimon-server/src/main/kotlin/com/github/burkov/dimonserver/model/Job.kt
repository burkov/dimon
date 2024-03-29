package com.github.burkov.dimonserver.model

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity(name = "Jobs")
data class Job(
        @Id val id: Long = -1,
        @Column(name = "worker_id") val workerId: String = "",
        @Column(name = "batch_id") val batchId: Long? = null,
        val params: String = "",
        @Column(name = "due_to") val dueTo: LocalDateTime = LocalDateTime.MIN,
        @Column(name = "retry_count") val retry_count: Int = -1,
        val context: String = "",
        val jobHash: String = ""
)

fun Job.toDTO() = JobDTO(id, workerId, params, dueTo, retry_count)

