package com.github.burkov.dimonserver.controller

import com.github.burkov.dimonserver.model.Job
import com.github.burkov.dimonserver.repository.JobsRepository
import com.github.burkov.dimonserver.repository.MarketDataRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.reactivestreams.Publisher
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.support.beans
import org.springframework.messaging.rsocket.RSocketRequester


@RestController
@RequestMapping("api")
class JobsRestController(val jobsRepository: JobsRepository, val context: ApplicationContext) {
    @GetMapping("/jobs")
    fun getAllJobs(): List<Job> = jobsRepository.findAll()

    @GetMapping("/jobs/contexts")
    fun getAllContexts(): Set<String> = jobsRepository.listDistinctContexts()

    @GetMapping("/current/{stock}")
    fun current(@PathVariable("stock") stock: String): Publisher<MarketData> {
        val rSocketRequester: RSocketRequester = context.getBean()

        return rSocketRequester
                .route("currentMarketData")
                .data(MarketDataRequest(stock))
                .retrieveMono(MarketData::class.java)
    }
}

@Controller
class MarketDataRSocketController(val marketDataRepository: MarketDataRepository) {
    @MessageMapping("currentMarketData")
    fun currentMarketData(marketDataRequest: MarketDataRequest): Mono<MarketData> {
        return marketDataRepository.getOne(marketDataRequest.stock)
    }
}

data class MarketData(val stock: String, val currentPrice: Int)
data class MarketDataRequest(val stock: String)
