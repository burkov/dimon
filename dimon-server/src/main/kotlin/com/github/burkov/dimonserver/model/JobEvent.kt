package com.github.burkov.dimonserver.model

import java.time.LocalDateTime


abstract class JobEvent {
    private val childClassName = this::class.java.simpleName!!
    private val jobEventClassName = JobEvent::class.java.simpleName!!

    init {
        require(childClassName.startsWith(jobEventClassName)) {
            "Successors of '$jobEventClassName' should be named like '${jobEventClassName}XXX'"
        }
    }

    @Suppress("unused")
    val type: String = childClassName.removePrefix(JobEvent::class.simpleName!!).toLowerCase()
}

abstract class JobEventWithDto : JobEvent() {
    abstract val job: JobDTO
}

data class JobEventInsert(override val job: JobDTO) : JobEventWithDto()
data class JobEventDelete(val jobId: Long) : JobEvent()
data class JobEventUpdate(override val job: JobDTO) : JobEventWithDto()
data class JobEventPing(val timestamp: LocalDateTime = LocalDateTime.now()) : JobEvent()
data class JobEventStarted(override val job: JobDTO) : JobEventWithDto()
data class JobEventCompleted(override val job: JobDTO) : JobEventWithDto()
data class JobEventCompletedInstantly(override val job: JobDTO) : JobEventWithDto()