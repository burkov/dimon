package com.github.burkov.dimonserver

import com.github.burkov.dimonserver.service.DispatcherMonitorService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class DimonConsoleRunner(val dispatcherMonitorService: DispatcherMonitorService) : CommandLineRunner {
    private val log = LoggerFactory.getLogger(DimonConsoleRunner::class.java)
    override fun run(vararg args: String?) {
        dispatcherMonitorService.stream.subscribe { ev ->
            log.info(ev.toString())
        }
    }
}