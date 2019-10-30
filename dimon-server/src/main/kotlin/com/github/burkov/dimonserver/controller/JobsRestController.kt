package com.github.burkov.dimonserver.controller

import com.github.burkov.dimonserver.model.Job
import com.github.burkov.dimonserver.repository.JobsRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("api")
class JobsRestController(val jobsRepository: JobsRepository) {
    @GetMapping("/jobs")
    fun getAllJobs(): List<Job> = jobsRepository.findAll()

    @GetMapping("/jobs/contexts")
    fun getAllContexts(): Set<String> = jobsRepository.listDistinctContexts()
}

