package com.github.burkov.dimonserver.model

import java.time.LocalDateTime

data class JobDTO(
        val id: Long,
        val workerId: String,
        val params: String,
        val dueTo: LocalDateTime,
        val retryCount: Int
)