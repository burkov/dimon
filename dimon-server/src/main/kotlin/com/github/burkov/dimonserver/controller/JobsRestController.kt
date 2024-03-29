package com.github.burkov.dimonserver.controller

import com.github.burkov.dimonserver.model.Job
import com.github.burkov.dimonserver.model.JobEvent
import com.github.burkov.dimonserver.repository.JobsRepository
import com.github.burkov.dimonserver.service.DispatcherMonitorService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux


@RestController
@RequestMapping
class JobsRestController(val jobsRepository: JobsRepository) {
    @GetMapping("/jobs")
    fun getAllJobs(): Flux<Job> = Flux.fromIterable(jobsRepository.findAll())

    @GetMapping("/jobs/contexts")
    fun getAllContexts(): Flux<String> = Flux.fromIterable(jobsRepository.listDistinctContexts())
}

@Controller
class JobsRSocketController(val dispatcherMonitorService: DispatcherMonitorService) {
    @MessageMapping("jobs-stream")
    fun jobsStream(): Flux<JobEvent> {
        return dispatcherMonitorService.stream
    }
}