package com.github.burkov.dimonserver.service

import com.github.burkov.dimonserver.model.Job
import com.github.burkov.dimonserver.repository.JobsRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap


data class CacheUpdateResult(
        val deletedIds: Set<Long>,
        val updatedJobs: Collection<Job>,
        val insertedJobs: Collection<Job>
)

typealias UpdateListener = () -> Unit

@Service
class JobsTableCacheService(val jobsRepository: JobsRepository) {
    private val cache = ConcurrentHashMap<Long, Job>()
    private val log = LoggerFactory.getLogger(javaClass)
    private val updateListeners = mutableListOf<UpdateListener>()

    fun lookupCached(id: Long): Job? {
        return cache[id] ?: run {
            //            log.info("Cache miss: $id not found in ${cache.size} cached jobs")
            null
        }
    }

    fun addOnCacheUpdateListener(block: UpdateListener) = updateListeners.add(block)

    fun updateCache(): CacheUpdateResult {
        val prevState = cache.toMap()
        cache.clear()
        jobsRepository
                .findAllByDueToBeforeOrderByDueToDesc()
                .associateByTo(cache) { it.id }
        updateListeners.forEach { it.invoke() }
        val prevStateIds = prevState.keys

        return CacheUpdateResult(
                deletedIds = prevStateIds - cache.keys,
                insertedJobs = cache
                        .filterKeys { it !in prevStateIds }
                        .values,
                updatedJobs = cache
                        .filter { (id, job) -> id in prevStateIds && job != prevState[id] }
                        .values
        )
    }
}