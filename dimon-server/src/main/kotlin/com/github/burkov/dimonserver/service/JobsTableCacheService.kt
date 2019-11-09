package com.github.burkov.dimonserver.service

import com.github.burkov.dimonserver.model.Job
import com.github.burkov.dimonserver.repository.JobsRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap


data class CacheUpdateResult(
        val updatedJobs: Collection<Job>,
        val insertedJobs: Collection<Job>
)

@Service
class JobsTableCacheService(val jobsRepository: JobsRepository) {
    private val cache = ConcurrentHashMap<Long, Job>()
    private val log = LoggerFactory.getLogger(javaClass)

    fun lookupCached(id: Long): Job? = cache[id]

    fun updateCache(): CacheUpdateResult {
        val (prevState, currState) = synchronized(cache) {
            val prevState = cache.toMap()
            cache.clear()
            jobsRepository
                    .findAllByDueToBeforeOrderByDueToDesc()
                    .associateByTo(cache) { it.id }
            prevState to cache.toMap()
        }
        val prevStateIds = prevState.keys

        return CacheUpdateResult(
                insertedJobs = currState
                        .filterKeys { it !in prevStateIds }
                        .values,
                updatedJobs = currState
                        .filter { (id, job) -> id in prevStateIds && job != prevState[id] }
                        .values
        )
    }
}