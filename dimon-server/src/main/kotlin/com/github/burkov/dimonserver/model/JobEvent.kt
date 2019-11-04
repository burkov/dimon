package com.github.burkov.dimonserver.model


abstract class JobEvent {
    private val childClassName = this::class.java.simpleName!!
    private val jobEventClassName = JobEvent::class.java.simpleName!!

    init {
        require(childClassName.startsWith(jobEventClassName)) {
            "Successors of '$jobEventClassName' should be named like '${jobEventClassName}XXX'"
        }
    }

    val type: String = childClassName.removePrefix(JobEvent::class.simpleName!!).toLowerCase()
}

data class JobEventInsert(val job: Job) : JobEvent()
data class JobEventDelete(val jobId: Long) : JobEvent()
data class JobEventUpdate(val job: Job) : JobEvent()
data class JobEventSnapshot(val jobs: List<Job>) : JobEvent()