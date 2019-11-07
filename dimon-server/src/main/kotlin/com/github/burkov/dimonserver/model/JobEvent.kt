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

data class JobEventInsert(val job: JobDTO) : JobEvent()
data class JobEventDelete(val jobId: Long) : JobEvent()
data class JobEventUpdate(val job: JobDTO) : JobEvent()
data class JobEventSnapshot(val jobs: List<JobDTO>) : JobEvent()
data class JobEventPing(val timestamp: LocalDateTime = LocalDateTime.now()) : JobEvent()
data class JobEventStarted(val job: JobDTO) : JobEvent()
data class JobEventCompleted(val job: JobDTO) : JobEvent()
data class JobEventCompletedInstantly(val job: JobDTO) : JobEvent()