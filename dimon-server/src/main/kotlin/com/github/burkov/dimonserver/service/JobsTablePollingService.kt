package com.github.burkov.dimonserver.service

import com.github.burkov.dimonserver.model.*
import com.github.burkov.dimonserver.repository.JobsRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.DirectProcessor
import reactor.core.publisher.Flux
import java.time.Duration
import javax.annotation.PostConstruct

@Service
class JobsTablePollingService(val jobsRepository: JobsRepository) {
    private val processor: DirectProcessor<JobEvent> = DirectProcessor.create()
    val stream: Flux<JobEvent> = Flux.from(processor)

    private val log = LoggerFactory.getLogger(JobsTablePollingService::class.java)
    private val jobs = mutableListOf<Job>()

    fun refine(jobPartialData: JobPartialData): JobDTO? {
        infix fun List<Job>.takeIfExactlyOneOrElse(additionalLookup: () -> Job?): Job? = when {
            this.size == 1 -> this.single()
            this.size > 1 -> {
                log.warn("More than one job found by $jobPartialData")
                null
            }
            else -> additionalLookup()
        }

        return synchronized(jobs) {
            val foundInCache = jobs.filter { jobPartialData.equalsToJob(it) }
            foundInCache takeIfExactlyOneOrElse {
                log.info("Cache miss, looking up in DB")
                val foundInDb = jobsRepository.findByWorkerIdAndParams(jobPartialData.workerId, jobPartialData.params)
                foundInDb takeIfExactlyOneOrElse {
                    log.warn("Was not able to find Job by $jobPartialData neither in cache nor in DB")
                    null
                }
            }
        }?.toDTO()
    }

    @PostConstruct
    private fun postConstruct() {
        Flux.interval(Duration.ofMillis(1000))
                .subscribe {
                    log.info(Thread.currentThread().name)
                    synchronized(jobs) {
                        // update cache
                    }
                }
    }

//    @Scheduled(fixedDelay = 1000)
//    private fun pollTable() {
//        val prevState = jobs.toList()
//        jobs.clear()
//        jobs.addAll(jobsRepository.findAllByDueToBeforeOrderByDueToDesc())
//        val difference = stateDifference(prevState, jobs)
//        for (subscriber in subscribers) {
//            when {
//                subscriber.sink.isCancelled -> {
//                    log.info("Subscription was cancelled")
//                    subscribers.remove(subscriber)
//                }
//                !subscriber.snapshotSent -> {
//                    subscriber.sink.next(JobEventSnapshot(jobs.map { it.toDTO() }))
//                    subscriber.snapshotSent = true
//                }
//                else -> difference.forEach { subscriber.sink.next(it) }
//            }
////            if (!subscriber.sink.isCancelled) subscriber.sink.next(JobEventTablePoll(timestamp = LocalDateTime.now()))
//        }
//    }

    private fun stateDifference(prevState: List<Job>, currState: List<Job>): List<JobEvent> {
        val prevMap = prevState.associateBy { it.id }
        val currMap = currState.associateBy { it.id }
        val prevIds = prevMap.keys
        val currIds = currMap.keys
        val deleted = prevIds - currIds
        val new = currIds - prevIds
        val updated = currMap
                .filterKeys { it !in new }
                .filterValues { it != prevMap[it.id] }
                .values
        return listOf(
                deleted.map { JobEventDelete(jobId = it) },
                new.map { JobEventInsert(currMap.getValue(it).toDTO()) },
                updated.map { JobEventUpdate(it.toDTO()) }
        ).flatten().also { println("diff ${it.size}, deleted: ${deleted.size}, new: ${new.size}, updated: ${updated.size}") }
    }
}