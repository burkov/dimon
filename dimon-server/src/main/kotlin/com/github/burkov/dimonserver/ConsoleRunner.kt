package com.github.burkov.dimonserver

import com.github.burkov.dimonserver.service.JobsTablePollingService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class DimonConsoleRunner(val tablePollingService: JobsTablePollingService) : CommandLineRunner {
    private val log = LoggerFactory.getLogger(DimonConsoleRunner::class.java)
    override fun run(vararg args: String?) {
        log.info("Event stream subscriber test")
        tablePollingService.jobEventsStream(false).subscribe { event ->
            log.info("Event: ${event.type} $event")
        }
        log.info("Done")
    }
}