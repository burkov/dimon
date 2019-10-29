package com.github.burkov.dpmonserver.repository

import com.github.burkov.dpmonserver.model.Job
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface JobsRepository : JpaRepository<Job, Long> {
    @Query("select distinct context from Jobs", nativeQuery = true)
    fun listDistinctContexts(): Set<String>

}