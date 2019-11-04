package com.github.burkov.dimonserver

import com.github.burkov.dimonserver.repository.JobsRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import reactor.core.publisher.DirectProcessor
import reactor.core.publisher.Flux


@SpringBootApplication
class DimonConsoleRunner(val jpaRepository: JobsRepository) : CommandLineRunner {
    override fun run(vararg args: String?) {


    }
}