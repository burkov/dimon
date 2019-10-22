package com.github.burkov.dpmonserver.controller

import com.github.burkov.dpmonserver.models.Job
import com.github.burkov.dpmonserver.repository.JobsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class JobsController(@Autowired private val jobsRepository: JobsRepository) {
    fun getAllJobs(): List<Job> = jobsRepository.findAll()
}