package com.github.burkov.dimonserver.repository

import com.github.burkov.dimonserver.model.Job
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime


@Repository
interface JobsRepository : JpaRepository<Job, Long> {
    @Query("select distinct context from Jobs")
    fun listDistinctContexts(): Set<String>

    fun findAllByDueToBeforeOrderByDueToDesc(dueTo: LocalDateTime = LocalDateTime.now().plusHours(1)): List<Job>

    fun findByWorkerIdAndParams(workerId: String, params: String): List<Job>
}