package com.github.burkov.dimonserver.controller

import com.github.burkov.dimonserver.model.Job
import com.github.burkov.dimonserver.repository.JobsRepository
import org.reactivestreams.Publisher
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.Exception


//@RestController
//@RequestMapping("api")
//class JobsRestController(val jobsRepository: JobsRepository, val requester: Mono<RSocketRequester>) {
//    @GetMapping("/jobs")
//    fun getAllJobs(): List<Job> = jobsRepository.findAll()
//
//    @GetMapping("/jobs/contexts")
//    fun getAllContexts(): Set<String> = jobsRepository.listDistinctContexts()
//
//    @GetMapping("/test")
//    fun testWsCall(): String {
//        requester.block()!!.route("hello").retrieveFlux(String::class.java)
//        return "works"
//    }
//}

@Controller
class HelloWorldRSocketController {
    @MessageMapping("hello")
    fun sayHello(): Flux<String> {
        return Flux.just("Hello RSocket")
    }
}