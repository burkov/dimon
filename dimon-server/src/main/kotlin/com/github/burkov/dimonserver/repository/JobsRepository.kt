package com.github.burkov.dimonserver.repository

import com.github.burkov.dimonserver.model.Job
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux


@Repository
interface JobsRepository : JpaRepository<Job, Long> {
    @Query("select distinct context from Jobs")
    fun listDistinctContexts(): Set<String>


//    fun listDistinctContextsReactive(): Flux<String> {
//        return Flux.fromIterable(listDistinctContexts())
//    }
}