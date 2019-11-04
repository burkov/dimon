package com.github.burkov.dimonserver.service

import com.github.burkov.dimonserver.model.*
import com.github.burkov.dimonserver.repository.JobsRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import java.util.concurrent.ConcurrentHashMap

private data class TablePollingServiceSubscriber(
        val emitter: FluxSink<JobEvent>,
//        val flux: Flux<JobEvent>,
        var snapshotSent: Boolean
)

@Service
class JobsTablePollingService(val jobsRepository: JobsRepository) {
    /**
     * @param sendSnapshot true if snapshot should be sent to subscriber
     */
    fun subscribe(sendSnapshot: Boolean): Flux<JobEvent> {
        return Flux.create<JobEvent> { emitter ->
            subscribers[emitter] = TablePollingServiceSubscriber(emitter, !sendSnapshot)
        }
    }

//    fun unsubscribe(flux: Flux<JobEvent>) {
//        val subscriberData = subscribers[flux]
//        requireNotNull(subscriberData)
//        subscriberData.emitter.complete()
//        subscribers.remove(flux)
//    }

    private val subscribers = ConcurrentHashMap<FluxSink<JobEvent>, TablePollingServiceSubscriber>()
    private val jobs = mutableListOf<Job>()

    @Scheduled(fixedDelay = 1000)
    private fun pollTable() {
        if (subscribers.size == 0) return
        val prevState = jobs.toList()
        jobs.clear()
        jobs.addAll(jobsRepository.findAll())
        val difference = difference(prevState, jobs)
        subscribers.values.forEach { subscriber ->
            val emitter = subscriber.emitter
            if (!subscriber.snapshotSent) {
                emitter.next(JobEventSnapshot(jobs))
                subscriber.snapshotSent = true
            } else difference.forEach { emitter.next(it) }
        }
    }

    private fun difference(prevState: List<Job>, currState: List<Job>): List<JobEvent> {
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
                new.map { JobEventInsert(currMap.getValue(it)) },
                updated.map { JobEventUpdate(it) }
        ).flatten()
    }
}