package com.github.burkov.dimonserver

import com.github.burkov.dimonserver.model.JobEventCompleted
import com.github.burkov.dimonserver.model.JobEventCompletedInstantly
import com.github.burkov.dimonserver.model.JobEventScheduled
import com.github.burkov.dimonserver.model.JobEventStarted
import com.github.burkov.dimonserver.service.DispatcherMonitorService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class DimonConsoleRunner(val dispatcherMonitorService: DispatcherMonitorService) : CommandLineRunner {
    private val log = LoggerFactory.getLogger(DimonConsoleRunner::class.java)
    override fun run(vararg args: String?) {
//        var scheduled = 0
//        var started = 0
//        var completed = 0
//        var completedInstantly = 0
//        var other = 0
//        dispatcherMonitorService.stream.subscribe { ev ->
//            when (ev) {
//                is JobEventScheduled -> scheduled++
//                is JobEventStarted -> started++
//                is JobEventCompleted -> completed++
//                is JobEventCompletedInstantly -> completedInstantly++
//                else -> other++
//            }
//            val stats = listOf(scheduled, started, completed, completedInstantly, other).joinToString(separator = "/") {
//                it.toString().padStart(5, ' ')
//            }
//            log.info("sc/st/co/ci/other $stats")
//        }
    }
}