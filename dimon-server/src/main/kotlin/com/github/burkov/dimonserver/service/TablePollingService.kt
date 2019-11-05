package com.github.burkov.dimonserver.service

import com.github.burkov.dimonserver.model.*
import com.github.burkov.dimonserver.repository.JobsRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentLinkedQueue

private data class ServiceSubscriber(
        val sink: FluxSink<JobEvent>,
        var snapshotSent: Boolean
)

@Service
class JobsTablePollingService(val jobsRepository: JobsRepository) {
    /**
     * @param sendSnapshot true if snapshot should be sent to subscriber
     */
    fun jobEventsStream(sendSnapshot: Boolean): Flux<JobEvent> {
        log.info("+1 Subscriber, total: ${subscribers.size + 1}")
        return Flux.create<JobEvent> { sink ->
            subscribers.add(ServiceSubscriber(sink, !sendSnapshot))
        }
    }

    private val log = LoggerFactory.getLogger(JobsTablePollingService::class.java)
    private val subscribers = ConcurrentLinkedQueue<ServiceSubscriber>()
    private val jobs = mutableListOf<Job>()

    @Scheduled(fixedDelay = 1000)
    private fun pollTable() {
        if (subscribers.size == 0) {
            jobs.clear()
            return
        }
        val prevState = jobs.toList()
        jobs.clear()
        jobs.addAll(jobsRepository.findAllByDueToBeforeOrderByDueToDesc())
        val difference = stateDifference(prevState, jobs)
        for (subscriber in subscribers) {
            when {
                subscriber.sink.isCancelled -> {
                    log.info("Subscription was cancelled")
                    subscribers.remove(subscriber)
                }
                !subscriber.snapshotSent -> {
                    subscriber.sink.next(JobEventSnapshot(jobs.map { it.toDTO() }))
                    subscriber.snapshotSent = true
                }
                else -> difference.forEach { subscriber.sink.next(it) }
            }
//            if (!subscriber.sink.isCancelled) subscriber.sink.next(JobEventTablePoll(timestamp = LocalDateTime.now()))
        }
    }

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