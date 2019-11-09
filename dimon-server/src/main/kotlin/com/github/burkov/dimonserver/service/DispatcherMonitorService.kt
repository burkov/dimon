package com.github.burkov.dimonserver.service

import com.github.burkov.dimonserver.model.*
import com.github.burkov.dimonserver.repository.JobsRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reactor.core.publisher.DirectProcessor
import reactor.core.publisher.Flux

@Service
class DispatcherMonitorService(val jobsRepository: JobsRepository, val jobsTableCacheService: JobsTableCacheService) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val processor: DirectProcessor<JobEvent> = DirectProcessor.create()
    private val events = mutableListOf<JobEvent>()
    val stream: Flux<JobEvent> = Flux.from(processor)

    fun reportJobEvent(jobEvent: JobEvent) {
        when (jobEvent) {
            is JobEventStarted, is JobEventCompleted -> synchronized(events) {
                events.add(jobEvent)
            }
            else -> log.error("Reporting of event $jobEvent is not supported")
        }
    }

    @Scheduled(fixedDelayString = "\${dimon.stateRefreshIntervalMs}")
    private fun refreshState() {
        val accumulatedEvents = synchronized(events) {
            events.toList().let { list ->
                events.clear()
                list.filterIsInstance<JobEventWithDto>().takeIf { it.isNotEmpty() }
            }
        } ?: return
        val stateChanges = jobsTableCacheService.updateCache()
        val eventIds = accumulatedEvents.mapTo(mutableSetOf()) { it.job.id }
        val scheduled = stateChanges.insertedJobs.filter { it.id !in eventIds }
        accumulatedEvents
                .groupBy { it.job.id }
                .forEach { (jobId, values) -> refreshStateHandleEventsGroup(jobId, values) }
        scheduled.forEach { processor.onNext(JobEventScheduled(it.toDTO())) }
    }

    private fun refreshStateHandleEventsGroup(jobId: Long, values: List<JobEventWithDto>) {
        val stillAlive = jobsTableCacheService.lookupCached(jobId) != null
        val starts = values.filterIsInstance<JobEventStarted>()
        val stops = values.filterIsInstance<JobEventCompleted>()
        val otherTypes = values.filter { it !is JobEventCompleted && it !is JobEventStarted }
        fun String.withGroupId() = "Event group id=${jobId} $this"
        if (otherTypes.isNotEmpty()) log.warn("has events of unexpected types: $otherTypes".withGroupId())
        if (stops.size > 1) log.warn("was reported as stopped more than 1 time ($stops)".withGroupId())
        if (starts.size > 1) log.warn("was reported as started more than 1 time ($starts)".withGroupId())

        when {
            starts.isEmpty() && stops.isEmpty() -> log.warn("contains only unexpected event types".withGroupId())
            stillAlive && stops.isNotEmpty() -> {
                log.error("was reported as completed ($stops) but it is still in DB".withGroupId())
                // send all event without any processing, some weird shit is happening, don't mask it
                values.forEach { processor.onNext(it) }
            }
            stillAlive -> processor.onNext(starts.first())
            !stillAlive && starts.isNotEmpty() -> processor.onNext(JobEventCompletedInstantly(starts.first().job))
            else -> processor.onNext(stops.first())
        }
    }
}