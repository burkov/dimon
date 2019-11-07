package com.github.burkov.dimonserver

import com.github.burkov.dimonserver.service.JobsTablePollingService
import com.github.burkov.dimonserver.service.LogFacesListenerService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class DimonConsoleRunner(val logFacesListenerService: LogFacesListenerService) : CommandLineRunner {
    private val log = LoggerFactory.getLogger(DimonConsoleRunner::class.java)
    override fun run(vararg args: String?) {
        logFacesListenerService.stream.subscribe { ev ->
            log.info(ev.toString())
        }
    }
}