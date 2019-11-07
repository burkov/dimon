package com.github.burkov.dimonserver.service

import com.github.burkov.dimonserver.model.*
import com.github.burkov.dimonserver.repository.JobsRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.DirectProcessor
import reactor.core.publisher.Flux
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

@Service
class JobsTablePollingService(val jobsTableCacheService: JobsTableCacheService) {
    private val processor: DirectProcessor<JobEvent> = DirectProcessor.create()
    val stream: Flux<JobEvent> = Flux.from(processor)

    private val log = LoggerFactory.getLogger(JobsTablePollingService::class.java)

    @PostConstruct
    private fun postConstruct() {
        Flux.interval(Duration.ofMillis(1000))
                .subscribe {
                    val (deletedIds, updatedJobs, insertedJobs) = jobsTableCacheService.updateCache()
                    listOf(
                            deletedIds.map { JobEventDelete(it) },
                            updatedJobs.map { JobEventUpdate(it.toDTO()) },
                            insertedJobs.map { JobEventInsert(it.toDTO()) }
                    ).flatten().forEach {
                        processor.onNext(it)
                    }
                }
    }
}